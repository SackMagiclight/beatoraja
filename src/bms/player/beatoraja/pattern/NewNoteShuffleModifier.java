package bms.player.beatoraja.pattern;

import java.util.*;

import bms.model.*;

/**
 * タイムラインごとにノーツを入れ替えるクラス
 * @author KEH
 *
 */
public class NewNoteShuffleModifier extends PatternModifier {

	private Randomizer randomizer;
	private boolean isScratchLaneModify;

	public NewNoteShuffleModifier(Random r, Mode mode) {
		this(r, 0, mode);
	}

	public NewNoteShuffleModifier(Random r, int type, Mode mode) {
		super(r.assist);
		this.isScratchLaneModify = r.isScratchLaneModify;
		this.randomizer = Randomizer.create(r, type, mode);
	}

	@Override
	public List<PatternModifyLog> modify(BMSModel model) {
		List<PatternModifyLog> log = new ArrayList<PatternModifyLog>();
		randomizer.setModifyLanes(getKeys(model.getMode(), isScratchLaneModify));
		for(TimeLine tl : model.getAllTimeLines()) {
			if (tl.existNote() || tl.existHiddenNote()) {
				log.add(new PatternModifyLog(tl.getSection(), randomizer.permutate(tl)));
			}
		}
		return log;
	}

}
