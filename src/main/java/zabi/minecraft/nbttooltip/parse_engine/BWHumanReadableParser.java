package zabi.minecraft.nbttooltip.parse_engine;

import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import zabi.minecraft.nbttooltip.NBTTooltip;
import zabi.minecraft.nbttooltip.config.ModConfig;

import java.util.List;

public class BWHumanReadableParser implements NbtTagParser {
	
	private static final int line_split_threshold = 30;

	@Override
	public void parseTagToList(List<Text> list, @Nullable Tag tag, boolean split) {
		if (tag == null) {
			list.add(new LiteralText("No NBT tag"));
		} else {
			unwrapTag(list, tag, NBTTooltip.FORMAT, "", ModConfig.INSTANCE.compress?"":"  ", split);
		}
	}
	
	private void unwrapTag(List<Text> tooltip, Tag base, String pad, String tagName, String padIncrement, boolean splitLongStrings) {
		if (base instanceof CompoundTag) {
			addCompoundToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else if (base instanceof AbstractListTag) {
			addListToTooltip(tooltip, base, pad, padIncrement, splitLongStrings);
		} else {
			addValueToTooltip(tooltip, base, tagName, pad, splitLongStrings);
		}
	}
	
	private void addCompoundToTooltip(List<Text> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		CompoundTag tag = (CompoundTag) base;
		tag.getKeys().forEach(s -> {
			boolean nested = (tag.get(s) instanceof AbstractListTag) || (tag.get(s) instanceof CompoundTag);
			if (nested) {
				tooltip.add(new LiteralText(pad+s+": {"));
				unwrapTag(tooltip, tag.get(s), pad+padIncrement, s, padIncrement, splitLongStrings);
				tooltip.add(new LiteralText(pad+"}"));
			} else {
				addValueToTooltip(tooltip, tag.get(s), s, pad, splitLongStrings);
			}
		});
	}
	
	private void addListToTooltip(List<Text> tooltip, Tag base, String pad, String padIncrement, boolean splitLongStrings) {
		AbstractListTag<?> tag = (AbstractListTag<?>) base;
		int index = 0;
		for (Tag nbtnext : tag) {
			if (nbtnext instanceof AbstractListTag || nbtnext instanceof CompoundTag) {
				tooltip.add(new LiteralText(pad + "[" + index + "]: {"));
				unwrapTag(tooltip, nbtnext, pad + padIncrement, "", padIncrement, splitLongStrings);
				tooltip.add(new LiteralText(pad + "}"));
			} else {
				addValueToTooltip(tooltip, nbtnext, "[" + index + "]", pad, splitLongStrings);
			}
			index++;
		}
	}
	
	private static void addValueToTooltip(List<Text> tooltip, Tag nbt, String name, String pad, boolean splitLongStrings) {
		String toBeAdded = nbt.toString();
		if (!splitLongStrings || toBeAdded.length() < line_split_threshold) {
			tooltip.add(new LiteralText(pad+name+": "+ nbt));
		} else {
			int added = 0;
			tooltip.add(new LiteralText(pad+name+":"));
			while (added < toBeAdded.length()) {
				int nextChunk = Math.min(line_split_threshold, toBeAdded.length() - added);
				String sb = new StringBuilder()
						.append(Formatting.AQUA).append("|")
						.append(Formatting.RESET).append(pad)
						.append("   ")
						.append(toBeAdded, added, added + nextChunk).toString();
				tooltip.add(new LiteralText(sb));
				added += nextChunk;
			}
		}
	}

}
