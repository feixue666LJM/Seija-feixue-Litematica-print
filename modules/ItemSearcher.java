package com.kijinseija.seija_printer.print_main.modules;

import com.kijinseija.seija_printer.events.render.Render3DEvent;
import com.kijinseija.seija_printer.print_main.printer.util.BlockReplaceUtils;
import com.kijinseija.seija_printer.print_main.printer.util.BlockUtil;
import com.kijinseija.seija_printer.print_main.printer.util.ScheVerifyMixinUtil;
import com.kijinseija.seija_printer.print_main.printer.util.SeijaTimer;
import com.kijinseija.seija_printer.settings.core.BlockPosSetting;
import com.kijinseija.seija_printer.settings.core.Color;
import com.kijinseija.seija_printer.settings.core.Setting;
import com.kijinseija.seija_printer.settings.core.SettingGroup;
import com.kijinseija.seija_printer.settings.impl.DoubleRangeSetting;
import com.kijinseija.seija_printer.settings.obj.DoubleRange;
import com.kijinseija.seija_printer.utils.player.ChatUtils;
import com.kijinseija.seija_printer.utils.player.InvUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/** Schematic material analyser and optional container material collector. */
public final class ItemSearcher extends ClientModule {
    private static final String CHAT_PREFIX = "[Seija Printer]";

    private final SettingGroup general = settings.getDefaultGroup();

    public final Setting<BlockPos> point1 = general.add(new BlockPosSetting.Builder()
        .name("block1")
        .build());
    public final Setting<BlockPos> point2 = general.add(new BlockPosSetting.Builder()
        .name("block2")
        .build());
    public final Setting<DoubleRange> stealDelay = general.add(new DoubleRangeSetting.Builder()
        .name("steal-delay")
        .sliderRange(0, 2000)
        .range(0, 2000)
        .defaultValue(100, 200)
        .build());

    private final Map<Block, Integer> blockInfo = new ConcurrentHashMap<>();
    private final Map<Block, Integer> stealBlockInfo = new ConcurrentHashMap<>();
    private final SeijaTimer stealTimer = new SeijaTimer(stealDelay.get()::nextRandom);

    private volatile Thread schematicAnalysis;
    private boolean containerWasOpen;
    private int containerSlot;

    public ItemSearcher() {
        super("Item Searcher", "Analyses missing schematic materials and collects them from containers");
    }

    public Map<Block, Integer> materialCounts() {
        return Map.copyOf(blockInfo);
    }

    /** Starts a fresh background analysis of the configured schematic region. */
    public void startAnalysis() {
        if (!isActive()) return;

        BlockPos first = point1.get();
        BlockPos second = point2.get();
        if (first == null || second == null) {
            ChatUtils.sendMsg(CHAT_PREFIX, Text.literal("Set both analysis corners first."));
            return;
        }

        Thread previous = schematicAnalysis;
        if (previous != null) previous.interrupt();

        Thread analysis = new Thread(() -> analysisArea(first.toImmutable(), second.toImmutable()),
            "seija-printer-material-analysis");
        analysis.setDaemon(true);
        schematicAnalysis = analysis;
        analysis.start();
    }

    /** Prints the most recent material analysis to client chat. */
    public void printItemList() {
        if (!isActive()) return;

        blockInfo.forEach((block, count) -> {
            MutableText message = Text.literal("Block: ")
                .append(Text.translatable(block.getTranslationKey()))
                .append(" Count: ");
            if (count / 64 > 0) {
                message.append(Text.literal(Integer.toString(count / 64)))
                    .append(Text.literal(" *64 ").styled(style -> style.withColor(0x87CAFF)))
                    .append("+ ");
            }
            message.append(Integer.toString(count % 64));
            ChatUtils.sendMsg(CHAT_PREFIX, message);
        });
    }

    private void analysisArea(BlockPos first, BlockPos second) {
        if (mc.world == null || mc.player == null || SchematicWorldHandler.getSchematicWorld() == null) {
            sendStatus("No world or schematic is loaded.");
            return;
        }

        blockInfo.clear();
        BlockPos min = new BlockPos(
            Math.min(first.getX(), second.getX()),
            Math.min(first.getY(), second.getY()),
            Math.min(first.getZ(), second.getZ())
        );
        BlockPos max = new BlockPos(
            Math.max(first.getX(), second.getX()),
            Math.max(first.getY(), second.getY()),
            Math.max(first.getZ(), second.getZ())
        );

        try {
            for (BlockPos position : BlockPos.iterate(min, max)) {
                if (Thread.currentThread().isInterrupted()) {
                    sendStatus("Analysis stopped.");
                    return;
                }
                if (mc.world == null || !BlockUtil.blockposFilter(position)) continue;

                Block schematicBlock = BlockReplaceUtils.getScheStateNonReplace(position).getBlock();
                if (BlockUtil.isCanPlaceInBlock(schematicBlock)) continue;
                if (ScheVerifyMixinUtil.isReplacedBlockEqual(
                    schematicBlock,
                    mc.world.getBlockState(position).getBlock()
                )) continue;

                blockInfo.merge(schematicBlock, 1, Integer::sum);
            }

            updateStealInfo();
            sendStatus("Analysis complete.");
        } catch (RuntimeException exception) {
            sendStatus("Analysis failed: " + exception.getMessage());
        } finally {
            if (Thread.currentThread() == schematicAnalysis) schematicAnalysis = null;
        }
    }

    private void sendStatus(String text) {
        mc.execute(() -> ChatUtils.sendMsg(CHAT_PREFIX, Text.literal(text)));
    }

    private void updateStealInfo() {
        stealBlockInfo.clear();
        stealBlockInfo.putAll(blockInfo);
        if (mc.player == null) return;

        for (int index = 0; index < mc.player.getInventory().size(); index++) {
            ItemStack stack = mc.player.getInventory().getStack(index);
            decreaseItemCount(needSteal(stack.getItem()), stack.getCount());
        }
    }

    private void decreaseItemCount(Block block, int count) {
        if (block == null || count <= 0) return;
        stealBlockInfo.computeIfPresent(block, (ignored, needed) -> {
            int remaining = needed - count;
            return remaining <= 0 ? null : remaining;
        });
    }

    private Block needSteal(Item item) {
        if (!(item instanceof BlockItem)) return null;

        for (Block schematicBlock : stealBlockInfo.keySet()) {
            List<Block> replacements = BlockReplaceUtils.INSTANCE.getReplaceBlocks(schematicBlock);
            if (replacements.isEmpty()) {
                if (schematicBlock.asItem().equals(item)) return schematicBlock;
            } else {
                for (Block replacement : replacements) {
                    if (replacement.asItem().equals(item)) return schematicBlock;
                }
            }
        }
        return null;
    }

    @Override
    public void onClientTick() {
        if (mc.player == null) return;

        ScreenHandler menu = mc.player.currentScreenHandler;
        boolean containerOpen = menu instanceof GenericContainerScreenHandler || menu instanceof ShulkerBoxScreenHandler;
        if (containerWasOpen && !containerOpen) updateStealInfo();
        containerWasOpen = containerOpen;

        if (containerOpen && InvUtils.findEmpty().found()) {
            if (stealTimer.passed(stealDelay.get().getCurrentRandom())) {
                stealTimer.reset();
                stealChest(menu);
            }
        } else {
            containerSlot = 0;
        }
    }

    private boolean stealChest(ScreenHandler menu) {
        int containerSlots = Math.max(0, menu.slots.size() - 36);
        while (containerSlot < containerSlots) {
            ItemStack stack = menu.getSlot(containerSlot).getStack();
            Block block = needSteal(stack.getItem());
            if (block != null) {
                decreaseItemCount(block, stack.getCount());
                InvUtils.shiftClick().slotId(containerSlot++);
                return true;
            }
            containerSlot++;
        }
        return false;
    }

    @Override
    public void onRender3d(Render3DEvent event) {
        BlockPos first = point1.get();
        BlockPos second = point2.get();
        if (first == null && second == null) return;

        Color firstLine = new Color(255, 80, 80, 180);
        Color firstFill = new Color(255, 80, 80, 60);
        Color secondLine = new Color(80, 80, 255, 180);
        Color secondFill = new Color(80, 80, 255, 60);
        Color regionLine = new Color(255, 255, 255, 200);

        if (first != null) {
            event.renderer.boxSides(first.getX(), first.getY(), first.getZ(),
                first.getX() + 1, first.getY() + 1, first.getZ() + 1, firstFill, 0);
            event.renderer.boxLines(first.getX(), first.getY(), first.getZ(),
                first.getX() + 1, first.getY() + 1, first.getZ() + 1, firstLine, 0);
        }
        if (second != null) {
            event.renderer.boxSides(second.getX(), second.getY(), second.getZ(),
                second.getX() + 1, second.getY() + 1, second.getZ() + 1, secondFill, 0);
            event.renderer.boxLines(second.getX(), second.getY(), second.getZ(),
                second.getX() + 1, second.getY() + 1, second.getZ() + 1, secondLine, 0);
        }
        if (first != null && second != null) {
            event.renderer.boxLines(
                Math.min(first.getX(), second.getX()),
                Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()),
                Math.max(first.getX(), second.getX()) + 1,
                Math.max(first.getY(), second.getY()) + 1,
                Math.max(first.getZ(), second.getZ()) + 1,
                regionLine,
                0
            );
        }
    }

    @Override
    public void onDeactivate() {
        Thread analysis = schematicAnalysis;
        if (analysis != null) analysis.interrupt();
        schematicAnalysis = null;
        blockInfo.clear();
        stealBlockInfo.clear();
        containerWasOpen = false;
        containerSlot = 0;
    }
}
