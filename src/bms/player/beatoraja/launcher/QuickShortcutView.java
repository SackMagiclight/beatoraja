package bms.player.beatoraja.launcher;

import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bms.player.beatoraja.PlayModeConfig.ControllerConfig;
import bms.player.beatoraja.MainController;
import bms.player.beatoraja.PlayerConfig;
import bms.player.beatoraja.skin.SkinHeader;
import bms.player.beatoraja.skin.SkinType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class QuickShortcutView implements Initializable {

	@FXML
	private Label nowControllerName;

	@FXML
	private Button player1Button;

	@FXML
	private Button player2Button;

	@FXML
	private ComboBox<String> player1ControllerName;

	@FXML
	private ComboBox<String> player2ControllerName;

	@FXML
	private ComboBox<String> player1SkinName7Keys;
	@FXML
	private ComboBox<String> player1SkinValue7Keys;

	@FXML
	private ComboBox<String> player2SkinName7Keys;
	@FXML
	private ComboBox<String> player2SkinValue7Keys;

	private PlayerConfig player;
	private SkinHeader[] skinHeaders;

	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	public void update(PlayerConfig player, SkinHeader[] skinHeaders) {
		this.player = player;
		if (this.player == null) {
			return;
		}

		this.skinHeaders = skinHeaders;
		if (this.skinHeaders == null) {
			return;
		}

		List<String> controllers = Arrays.asList(this.player.getMode14().getController()).stream()
				.map(config -> config.getName()).collect(Collectors.toList());
		player1ControllerName.setItems(FXCollections.observableArrayList(controllers));
		player1ControllerName.setValue(controllers.get(0));

		player2ControllerName.setItems(FXCollections.observableArrayList(controllers));
		player2ControllerName.setValue(controllers.size() > 1 ? controllers.get(1) : controllers.get(0));

		nowControllerName.setText(this.player.getMode7().getController()[0].getName());

		List<String> skinPropName7Keys = Arrays
				.asList(this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getProperties().getOption()).stream()
				.map(config -> config.name).collect(Collectors.toList());

		player1SkinName7Keys.setItems(FXCollections.observableArrayList(skinPropName7Keys));
		player2SkinName7Keys.setItems(FXCollections.observableArrayList(skinPropName7Keys));
	}

	@FXML
	public void setPlayer1SkinName7Keys() {
		String setVal = player1SkinName7Keys.getValue();
		if (setVal == null)
			return;

		SkinHeader h = Arrays.stream(this.skinHeaders)
				.filter(header -> header.getPath().hashCode() == Path.of(this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getPath()).hashCode())
				.findFirst().get();

		String[] options = Arrays.stream(h.getCustomOptions()).filter(cp -> cp.name.equals(setVal)).findFirst().get().contents;
		List<String> optionValues = Arrays.stream(options).map(op -> String.valueOf(op)).collect(Collectors.toList());
		player1SkinValue7Keys.setItems(FXCollections.observableArrayList(optionValues));
	}
	
	@FXML
	public void setPlayer2SkinName7Keys() {
		String setVal = player2SkinName7Keys.getValue();
		if (setVal == null)
			return;

		SkinHeader h = Arrays.stream(this.skinHeaders)
				.filter(header -> header.getPath().hashCode() == Path.of(this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getPath()).hashCode())
				.findFirst().get();

		String[] options = Arrays.stream(h.getCustomOptions()).filter(cp -> cp.name.equals(setVal)).findFirst().get().contents;
		List<String> optionValues = Arrays.stream(options).map(op -> String.valueOf(op)).collect(Collectors.toList());
		player2SkinValue7Keys.setItems(FXCollections.observableArrayList(optionValues));
	}

	@FXML
	public void setPlayer1() {
		/* controller */
		int index = 0;
		List<ControllerConfig> c = Arrays.asList(this.player.getMode14().getController());

		String setVal = player1ControllerName.getValue();
		for (; index < c.size(); index++) {
			if (c.get(index).getName().equals(setVal)) {
				break;
			}
		}

		ControllerConfig newController = c.get(index);
		this.player.getMode7().getController()[0].setName(newController.getName());
		this.player.getMode7().getController()[0].setKeyAssign(Arrays.copyOfRange(newController.getKeyAssign(), 0, 9));
		this.player.getMode7().getController()[0].setStart(newController.getStart());
		this.player.getMode7().getController()[0].setSelect(newController.getSelect());
		this.player.getMode7().getController()[0].setDuration(newController.getDuration());
		this.player.getMode7().getController()[0].setJKOC(newController.getJKOC());
		this.player.getMode7().getController()[0].setAnalogScratch(newController.isAnalogScratch());
		this.player.getMode7().getController()[0].setAnalogScratchMode(newController.getAnalogScratchMode());
		this.player.getMode7().getController()[0].setAnalogScratchThreshold(newController.getAnalogScratchThreshold());
		nowControllerName.setText(newController.getName());
		
		/* skin */
		SkinHeader h = Arrays.stream(this.skinHeaders)
				.filter(header -> header.getPath().hashCode() == Path.of(this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getPath()).hashCode())
				.findFirst().get();
		int optionIndex = 0;
		String[] contents = Arrays.stream(h.getCustomOptions()).filter(op -> op.name.equals(player1SkinName7Keys.getValue())).findFirst().get().contents;
		int[] options = Arrays.stream(h.getCustomOptions()).filter(op -> op.name.equals(player1SkinName7Keys.getValue())).findFirst().get().option;
		for (index = 0; index < contents.length; index++) {
				if(contents[index].equals(player1SkinValue7Keys.getValue())) {
					optionIndex = index;
					break;
				}
		}
		
		for (index = 0; index < this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getProperties().getOption().length; index++) {
			if(this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getProperties().getOption()[index].name.equals(player1SkinName7Keys.getValue())){
				this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getProperties().getOption()[index].value = options[optionIndex];
			}
		}
	}

	@FXML
	public void setPlayer2() {
		int index = 0;
		List<ControllerConfig> c = Arrays.asList(this.player.getMode14().getController());

		String setVal = player2ControllerName.getValue();
		for (; index < c.size(); index++) {
			if (c.get(index).getName().equals(setVal)) {
				break;
			}
		}

		ControllerConfig newController = c.get(index);
		this.player.getMode7().getController()[0].setName(newController.getName());
		this.player.getMode7().getController()[0]
				.setKeyAssign(Arrays.copyOfRange(newController.getKeyAssign(), 9, 18));
		this.player.getMode7().getController()[0].setStart(newController.getStart());
		this.player.getMode7().getController()[0].setSelect(newController.getSelect());
		this.player.getMode7().getController()[0].setDuration(newController.getDuration());
		this.player.getMode7().getController()[0].setJKOC(newController.getJKOC());
		this.player.getMode7().getController()[0].setAnalogScratch(newController.isAnalogScratch());
		this.player.getMode7().getController()[0].setAnalogScratchMode(newController.getAnalogScratchMode());
		this.player.getMode7().getController()[0].setAnalogScratchThreshold(newController.getAnalogScratchThreshold());
		nowControllerName.setText(newController.getName());
		
		/* skin */
		SkinHeader h = Arrays.stream(this.skinHeaders)
				.filter(header -> header.getPath().hashCode() == Path.of(this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getPath()).hashCode())
				.findFirst().get();
		int optionIndex = 0;
		String[] contents = Arrays.stream(h.getCustomOptions()).filter(op -> op.name.equals(player2SkinName7Keys.getValue())).findFirst().get().contents;
		int[] options = Arrays.stream(h.getCustomOptions()).filter(op -> op.name.equals(player2SkinName7Keys.getValue())).findFirst().get().option;
		for (index = 0; index < contents.length; index++) {
				if(contents[index].equals(player2SkinValue7Keys.getValue())) {
					optionIndex = index;
					break;
				}
		}
		
		for (index = 0; index < this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getProperties().getOption().length; index++) {
			if(this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getProperties().getOption()[index].name.equals(player2SkinName7Keys.getValue())){
				this.player.getSkin()[SkinType.PLAY_7KEYS.getId()].getProperties().getOption()[index].value = options[optionIndex];
			}
		}
	}

	public void commit() {
	}
}
