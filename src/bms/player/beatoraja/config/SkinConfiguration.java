package bms.player.beatoraja.config;

import bms.player.beatoraja.MainController;
import bms.player.beatoraja.MainState;
import static bms.player.beatoraja.skin.SkinProperty.*;

import bms.player.beatoraja.SkinConfig;
import bms.player.beatoraja.input.BMSPlayerInputProcessor;
import bms.player.beatoraja.skin.*;
import bms.player.beatoraja.skin.lr2.LR2SkinHeaderLoader;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SkinConfiguration extends MainState {

	private SkinConfigurationSkin skin;
	private SkinType type;
	private SkinConfig config;
	private List<SkinHeader> allSkins;
	private List<SkinHeader> availableSkins;
	private int selectedSkinIndex;
	private SkinHeader selectedSkinHeader;
	private List<CustomItemBase> customOptions;
	private int customOptionOffset;
	private int customOptionOffsetMax;
	private Skin selectedSkin;

	public SkinConfiguration(MainController main) {
		super(main);
	}

	public void create() {
		loadSkin(SkinType.SKIN_SELECT);
		skin = (SkinConfigurationSkin) getSkin();
		loadAllSkins();
		changeSkinType(SkinType.getSkinTypeById(skin.getDefaultSkinType()));
	}

	public void render() {

		if (main.getInputProcessor().isExitPressed()) {
			main.getInputProcessor().setExitPressed(false);
			main.saveConfig();
			main.changeState(MainController.STATE_SELECTMUSIC);
		}
	}

	public void input() {
		BMSPlayerInputProcessor input = main.getInputProcessor();
		int mov = -input.getScroll();
		input.resetScroll();
		if (mov != 0 && customOptions != null) {
			customOptionOffset = Math.max(0, Math.min(customOptionOffsetMax, customOptionOffset + mov));
		}
	}

	public int getImageIndex(int id) {
		if (SkinPropertyMapper.isSkinSelectTypeId(id)) {
			SkinType t = SkinPropertyMapper.getSkinSelectType(id);
			return type == t ? 1 : 0;
		}
		return super.getImageIndex(id);
	}

	public float getSliderValue(int id) {
		switch (id) {
		case SLIDER_SKINSELECT_POSITION:
			return (float)customOptionOffset / customOptionOffsetMax;
		}
		return super.getSliderValue(id);
	}

	public String getTextValue(int id) {
		switch (id) {
		case STRING_SKIN_NAME:
			return selectedSkinHeader != null ? selectedSkinHeader.getName() : "";
		case STRING_SKIN_AUTHOR:
			return selectedSkinHeader != null ? "" : "";
		default:
			if (SkinPropertyMapper.isSkinCustomizeCategory(id)) {
				int index = SkinPropertyMapper.getSkinCustomizeCategoryIndex(id);
				if (customOptions != null && index + customOptionOffset < customOptions.size()) {
					return customOptions.get(index + customOptionOffset).getCategoryName();
				}
				return "";
			}
			if (SkinPropertyMapper.isSkinCustomizeItem(id)) {
				int index = SkinPropertyMapper.getSkinCustomizeItemIndex(id);
				if (customOptions != null && index + customOptionOffset < customOptions.size()) {
					return customOptions.get(index + customOptionOffset).getDisplayValue();
				}
				return "";
			}
		}
		return super.getTextValue(id);
	}

	public void executeClickEvent(int id, int arg) {
		switch (id) {
		case BUTTON_CHANGE_SKIN:
			if (arg >= 0) {
				setNextSkin();
			} else {
				setPrevSkin();
			}
			break;
		default:
			if (SkinPropertyMapper.isSkinCustomizeButton(id)) {
				int index = SkinPropertyMapper.getSkinCustomizeIndex(id) + customOptionOffset;
				if (customOptions != null && index < customOptions.size()) {
					CustomItemBase item = customOptions.get(index);
					if (arg >= 0) {
						if (item.getvalue() < item.getMax()) {
							item.setValue(item.getvalue() + 1);
						} else {
							item.setValue(item.getMin());
						}
					} else {
						if (item.getvalue() > item.getMin()) {
							item.setValue(item.getvalue() - 1);
						} else {
							item.setValue(item.getMax());
						}
					}
				}
			}
			if (SkinPropertyMapper.isSkinSelectTypeId(id)) {
				SkinType t = SkinPropertyMapper.getSkinSelectType(id);
				changeSkinType(t);
			}
		}
	}

	private void changeSkinType(SkinType type) {
		this.type = type != null ? type : SkinType.PLAY_7KEYS;
		this.config = main.getPlayerConfig().getSkin()[this.type.getId()];
		availableSkins = new ArrayList<>();
		for (SkinHeader header : allSkins) {
			if (header.getSkinType() == type) {
				availableSkins.add(header);
			}
		}
		if (this.config != null) {
			int index = -1;
			for (int i = 0; i < availableSkins.size(); i++) {
				SkinHeader header = availableSkins.get(i);
				if (header != null && header.getPath().equals(Paths.get(config.getPath()))) {
					index = i;
				}
			}
			selectSkin(index);
		} else {
			selectSkin(-1);
		}
	}

	private void setNextSkin() {
		if (availableSkins.isEmpty()) {
			Logger.getGlobal().warning("利用可能なスキンがありません");
			return;
		}

		if (config == null) {
			config = new SkinConfig();
			main.getPlayerConfig().getSkin()[type.getId()] = config;
		}

		int index = selectedSkinIndex < 0 ? 0 : (selectedSkinIndex + 1) % availableSkins.size();
		config.setPath(availableSkins.get(index).getPath().toString());
		config.setProperties(new SkinConfig.Property());
		selectSkin(index);
	}

	private void setPrevSkin() {
		if (availableSkins.isEmpty()) {
			Logger.getGlobal().warning("利用可能なスキンがありません");
			return;
		}

		if (config == null) {
			config = new SkinConfig();
			main.getPlayerConfig().getSkin()[type.getId()] = config;
		}

		int index = selectedSkinIndex < 0 ? 0 : (selectedSkinIndex - 1 + availableSkins.size()) % availableSkins.size();
		config.setPath(availableSkins.get(index).getPath().toString());
		config.setProperties(new SkinConfig.Property());
		selectSkin(index);
	}

	private void selectSkin(int index) {
		selectedSkinIndex = index;
		if (index >= 0) {
			selectedSkinHeader = availableSkins.get(selectedSkinIndex);
			customOptions = new ArrayList<>();
			customOptionOffset = 0;
			if (config.getProperties() == null) {
				config.setProperties(new SkinConfig.Property());
			}
			updateCustomOptions();
			updateCustomFiles();
			updateCustomOffsets();
			customOptionOffsetMax = Math.max(0, customOptions.size() - skin.getCustomPropertyCount());
		} else {
			selectedSkinHeader = null;
			customOptions = null;
		}
	}

	private void updateCustomOptions() {
		for (SkinHeader.CustomOption option : selectedSkinHeader.getCustomOptions()) {
			int selection = -1;
			for(SkinConfig.Option o : config.getProperties().getOption()) {
				if (o.name.equals(option.name)) {
					int i = o.value;
					if(i != OPTION_RANDOM_VALUE) {
						for (int j = 0; j < option.option.length; j++) {
							if (option.option[j] == i) {
								selection = j;
								break;
							}
						}
					} else {
						selection = option.option.length;
					}
					break;
				}
			}
			if (selection < 0) {
				if (option.def != null) {
					for (int j = 0; j < option.option.length; j++) {
						if (option.contents[j].equals(option.def)) {
							selection = j;
							break;
						}
					}
				}
				if (selection < 0) {
					selection = 0;
				}
				setCustomOption(option.name, option.option[selection]);
			}
			String[] contentsAddedRandom = new String[option.contents.length + 1];
			for(int i = 0; i < option.contents.length; i++) {
				contentsAddedRandom[i] = option.contents[i];
			}
			contentsAddedRandom[option.contents.length] = "Random";
			int[] optionAddedRandom = new int[option.option.length + 1];
			for(int i = 0; i < option.option.length; i++) {
				optionAddedRandom[i] = option.option[i];
			}
			optionAddedRandom[option.option.length] = OPTION_RANDOM_VALUE;

			CustomOptionItem item = new CustomOptionItem(option.name, contentsAddedRandom, optionAddedRandom, selection);
			customOptions.add(item);
		}
	}

	private void updateCustomFiles() {
		for (SkinHeader.CustomFile file : selectedSkinHeader.getCustomFiles()) {
			String name = file.path.substring(file.path.lastIndexOf('/') + 1);
			if(file.path.contains("|")) {
				if(file.path.length() > file.path.lastIndexOf('|') + 1) {
					name = file.path.substring(file.path.lastIndexOf('/') + 1, file.path.indexOf('|')) + file.path.substring(file.path.lastIndexOf('|') + 1);
				} else {
					name = file.path.substring(file.path.lastIndexOf('/') + 1, file.path.indexOf('|'));
				}
			}
			final Path dirpath = Paths.get(file.path.substring(0, file.path.lastIndexOf('/')));
			if (!Files.exists(dirpath)) {
				continue;
			}
			try (DirectoryStream<Path> paths = Files.newDirectoryStream(dirpath,
					"{" + name.toLowerCase() + "," + name.toUpperCase() + "}")) {

				List<String> items = new ArrayList<>();
				for (Path path : paths) {
					items.add(path.getFileName().toString());
				}
				items.add("Random");
				String selection = null;
				for(SkinConfig.FilePath f : config.getProperties().getFile()) {
					if(f.name.equals(file.name)) {
						selection = f.path;
						break;
					}
				}
				if (selection == null && file.def != null) {
					// デフォルト値のファイル名またはそれに拡張子を付けたものが存在すれば使用する
					for (String item : items) {
						if (item.equalsIgnoreCase(file.def)) {
							selection = item;
							break;
						}
						int point = item.lastIndexOf('.');
						if (point != -1 && item.substring(0, point).equalsIgnoreCase(file.def)) {
							selection = item;
							break;
						}
					}
					setFilePath(file.name, selection);
				}
				if (selection == null) {
					selection = items.get(0);
					setFilePath(file.name, selection);
				}
				CustomFileItem item = new CustomFileItem(file.name, items, selection);
				customOptions.add(item);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateCustomOffsets() {
		for (SkinHeader.CustomOffset option : selectedSkinHeader.getCustomOffsets()) {
			final String[] values = {"x","y","w","h","r","a"};
			boolean[] b = new boolean[] { option.x, option.y, option.w, option.h, option.r, option.a };
			SkinConfig.Offset ofs = null;
			for(SkinConfig.Offset o : config.getProperties().getOffset()) {
				if(o.name.equals(option.name)) {
					ofs = o;
					break;
				}
			}
			if (ofs == null) {
				int length = config.getProperties().getOffset().length;
				SkinConfig.Offset[] offsets = Arrays.copyOf(config.getProperties().getOffset(), length + 1);
				offsets[length] = new SkinConfig.Offset();
				offsets[length].name = option.name;
				ofs = offsets[length];
				config.getProperties().setOffset(offsets);
			}
			int[] v = new int[] { ofs.x, ofs.y, ofs.w, ofs.h, ofs.r, ofs.a };
			for(int i = 0; i < 6; i++) {
				if(b[i]) {
					CustomOffsetItem item = new CustomOffsetItem(option.name, values[i], i, -9999, 9999, v[i]);
					customOptions.add(item);
				}
			}
		}
	}

	private void setCustomOption(String name, int value) {
		for (SkinConfig.Option option : config.getProperties().getOption()) {
			if (option.name.equals(name)) {
				option.value = value;
				return;
			}
		}
		// 既存のコンフィグデータに存在しない場合
		int length = config.getProperties().getOption().length;
		SkinConfig.Option[] options = Arrays.copyOf(config.getProperties().getOption(), length + 1);
		options[length] = new SkinConfig.Option();
		options[length].name = name;
		options[length].value = value;
		config.getProperties().setOption(options);
	}

	private void setFilePath(String name, String path) {
		for (SkinConfig.FilePath f : config.getProperties().getFile()) {
			if(f.name.equals(name)) {
				f.path = path;
				return;
			}
		}
		// 既存のコンフィグデータに存在しない場合
		int length = config.getProperties().getFile().length;
		SkinConfig.FilePath[] paths = Arrays.copyOf(config.getProperties().getFile(), length + 1);
		paths[length] = new SkinConfig.FilePath();
		paths[length].name = name;
		paths[length].path = path;
		config.getProperties().setFile(paths);
	}

	private void setCustomOffset(String name, int kind, int value) {
		for(SkinConfig.Offset offset : config.getProperties().getOffset()) {
			if(offset.name.equals(name)) {
				setOffset(offset, kind, value);
				return;
			}
		}
		// 既存のコンフィグデータに存在しない場合
		int length = config.getProperties().getOffset().length;
		SkinConfig.Offset[] offsets = Arrays.copyOf(config.getProperties().getOffset(), length + 1);
		offsets[length] = new SkinConfig.Offset();
		offsets[length].name = name;
		setOffset(offsets[length], kind, value);
		config.getProperties().setOffset(offsets);
	}

	private void setOffset(SkinConfig.Offset offset, int kind, int value) {
		switch (kind) {
		case 0:
			offset.x = value;
			break;
		case 1:
			offset.y = value;
			break;
		case 2:
			offset.w = value;
			break;
		case 3:
			offset.h = value;
			break;
		case 4:
			offset.r = value;
			break;
		case 5:
			offset.a = value;
			break;
		}
	}

	private void loadAllSkins() {
		allSkins = new ArrayList<SkinHeader>();
		List<Path> skinPaths = new ArrayList<>();
		scanSkins(Paths.get("skin"), skinPaths);
		for (Path path : skinPaths) {
			if (path.toString().toLowerCase().endsWith(".json")) {
				JSONSkinLoader loader = new JSONSkinLoader();
				SkinHeader header = loader.loadHeader(path);
				if (header != null) {
					allSkins.add(header);
				}
			} else {
				LR2SkinHeaderLoader loader = new LR2SkinHeaderLoader();
				try {
					SkinHeader header = loader.loadSkin(path, null);
					allSkins.add(header);
					// 7/14key skinは5/10keyにも加える
					if(header.getType() == SkinHeader.TYPE_LR2SKIN &&
							(header.getSkinType() == SkinType.PLAY_7KEYS || header.getSkinType() == SkinType.PLAY_14KEYS)) {
						header = loader.loadSkin(path, null);

						if(header.getSkinType() == SkinType.PLAY_7KEYS && !header.getName().toLowerCase().contains("7key")) {
							header.setName(header.getName() + " (7KEYS) ");
						} else if(header.getSkinType() == SkinType.PLAY_14KEYS && !header.getName().toLowerCase().contains("14key")) {
							header.setName(header.getName() + " (14KEYS) ");
						}
						header.setSkinType(header.getSkinType() == SkinType.PLAY_7KEYS ? SkinType.PLAY_5KEYS : SkinType.PLAY_10KEYS);
						allSkins.add(header);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void scanSkins(Path path, List<Path> paths) {
		if (Files.isDirectory(path)) {
			try (Stream<Path> sub = Files.list(path)) {
				sub.forEach((Path t) -> {
					scanSkins(t, paths);
				});
			} catch (IOException e) {
			}
		} else if (path.getFileName().toString().toLowerCase().endsWith(".lr2skin")
				|| path.getFileName().toString().toLowerCase().endsWith(".json")) {
			paths.add(path);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	private abstract static class CustomItemBase {
		protected final String categoryName;
		protected final int min;
		protected final int max;
		protected int value;
		protected String displayValue;

		public CustomItemBase(String categoryName, int count) {
			this.categoryName = categoryName;
			min = 0;
			max = count - 1;
		}

		public CustomItemBase(String categoryName, int min, int max) {
			this.categoryName = categoryName;
			this.min = min;
			this.max = max;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public int getMin() {
			return min;
		}

		public int getMax() {
			return max;
		}

		public int getvalue() {
			return value;
		}

		public String getDisplayValue() {
			return displayValue;
		}

		public abstract void setValue(int value);
	}

	private class CustomOptionItem extends CustomItemBase {
		String[] values;
		int[] options;

		public CustomOptionItem(String name, String[] items, int[] options, int index) {
			super(name, items.length);
			this.values = items;
			this.options = options;
			this.value = index;
			this.displayValue = values[index];
		}

		public void setValue(int i) {
			value = i;
			displayValue = values[value];
			setCustomOption(categoryName, options[value]);
		}
	}

	private class CustomFileItem extends CustomItemBase {
		List<String> displayValues;
		List<String> actualValues;

		public CustomFileItem(String name, List<String> paths, String selection) {
			super(name, paths.size());
			actualValues = paths;
			displayValues = new ArrayList<String>();
			int i=0;
			for (String path : paths) {
				int point = path.lastIndexOf('.');
				if (point >= 0) {
					displayValues.add(path.substring(0, point));
				} else {
					displayValues.add(path);
				}
				if (path.equals(selection)) {
					this.value = i;
					this.displayValue = displayValues.get(i);
				}
				i++;
			}
		}

		public void setValue(int i) {
			value = i;
			displayValue = displayValues.get(value);
			setFilePath(categoryName, actualValues.get(value));
		}
	}

	private class CustomOffsetItem extends CustomItemBase {
		String offsetName;
		int kind;

		public CustomOffsetItem(String offsetName, String kindName, int kind, int min, int max, int selection) {
			super(offsetName + " - " + kindName, min, max);
			this.offsetName = offsetName;
			this.kind = kind;
			this.value = selection;
			this.displayValue = String.valueOf(this.value);
		}

		public void setValue(int i) {
			value = i;
			displayValue = String.valueOf(value);
			setCustomOffset(offsetName, kind, value);
		}
	}
}
