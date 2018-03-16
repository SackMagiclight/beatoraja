package bms.player.beatoraja.select;

import bms.player.beatoraja.*;
import bms.player.beatoraja.select.bar.*;
import bms.player.beatoraja.song.SongData;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static bms.player.beatoraja.select.MusicSelector.SOUND_CHANGEOPTION;

public enum MusicSelectCommand {

    NEXT_MODE {
        @Override
        public void execute(MusicSelector selector) {
            int mode = 0;
            PlayerConfig config = selector.main.getPlayerConfig();
            for(;mode < MusicSelector.MODE.length && MusicSelector.MODE[mode] != config.getMode();mode++);
            config.setMode(MusicSelector.MODE[(mode + 1) % MusicSelector.MODE.length]);
            selector.getBarRender().updateBar();
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    NEXT_SORT {
        @Override
        public void execute(MusicSelector selector) {
            selector.setSort((selector.getSort() + 1) % BarSorter.values().length);
            selector.getBarRender().updateBar();
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    NEXT_LNMODE {
        @Override
        public void execute(MusicSelector selector) {
            PlayerConfig config = selector.main.getPlayerConfig();
            config.setLnmode((config.getLnmode() + 1) % 3);
            selector.getBarRender().updateBar();
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    RESET_REPLAY {
        @Override
        public void execute(MusicSelector selector) {
            if (selector.getBarRender().getSelected() instanceof SelectableBar) {
                boolean[] replays = ((SelectableBar) selector.getBarRender().getSelected()).getExistsReplayData();
                for (int i = 0; i < replays.length; i++) {
                    if (replays[i]) {
                        selector.setSelectedReplay(i);
                        return;
                    }
                }
            }
            selector.setSelectedReplay(-1);
        }
    },
    NEXT_REPLAY {
        @Override
        public void execute(MusicSelector selector) {
            Bar current = selector.getBarRender().getSelected();
            if (current != null && current instanceof SelectableBar) {
                boolean[] replays = ((SelectableBar) current).getExistsReplayData();
                for (int i = 1; i < replays.length; i++) {
                    final int selectedreplay = selector.getSelectedReplay();
                    if (replays[(i + selectedreplay) % replays.length]) {
                        selector.setSelectedReplay((i + selectedreplay) % replays.length);
                        selector.play(SOUND_CHANGEOPTION);
                        break;
                    }
                }
            }
        }
    },
    NEXT_RIVAL {
        @Override
        public void execute(MusicSelector selector) {
            PlayerInformation nowrival = selector.getRival();
            boolean match = (nowrival == null);
            for(PlayerInformation rival : selector.getRivals()) {
                if(match) {
                    nowrival = rival;
                    match = false;
                    break;
                }
                match = (nowrival == rival);
            }
            if(match) {
                nowrival = null;
            }
            selector.setRival(nowrival);
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    NEXT_TARGET {
        @Override
        public void execute(MusicSelector selector) {
            PlayerInformation nowrival = selector.getRival();
            boolean match = (nowrival == null);
            for(PlayerInformation rival : selector.getRivals()) {
                if(match) {
                    nowrival = rival;
                    match = false;
                    break;
                }
                match = (nowrival == rival);
            }
            if(match) {
                nowrival = null;
            }
            selector.setRival(nowrival);
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    NEXT_OPTION_1P {
        @Override
        public void execute(MusicSelector selector) {
            PlayerConfig config = selector.main.getPlayerConfig();
            config.setRandom((config.getRandom() + 1) % 10);
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    NEXT_OPTION_2P {
        @Override
        public void execute(MusicSelector selector) {
            PlayerConfig config = selector.main.getPlayerConfig();
            config.setRandom2((config.getRandom2() + 1) % 10);
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    NEXT_OPTION_DP {
        @Override
        public void execute(MusicSelector selector) {
            PlayerConfig config = selector.main.getPlayerConfig();
            config.setDoubleoption((config.getDoubleoption() + 1) % 4);
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    NEXT_GAUGE_1P {
        @Override
        public void execute(MusicSelector selector) {
            PlayerConfig config = selector.main.getPlayerConfig();
            config.setGauge((config.getGauge() + 1) % 6);
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    NEXT_HSFIX {
        @Override
        public void execute(MusicSelector selector) {
            PlayerConfig config = selector.main.getPlayerConfig();
            config.setFixhispeed((config.getFixhispeed() + 1) % 5);
            selector.play(SOUND_CHANGEOPTION);
        }
    },
    OPEN_WITH_EXPLORER {
        @Override
        public void execute(MusicSelector selector) {
            Bar current = selector.getBarRender().getSelected();
            try {
                if (Desktop.isDesktopSupported()) {
                    if(current instanceof SongBar && ((SongBar) current).existsSong()) {
                        Desktop.getDesktop().open(Paths.get(((SongBar) current).getSongData().getPath()).getParent().toFile());
                    } else if(current instanceof FolderBar) {
                        Desktop.getDesktop().open(Paths.get(((FolderBar) current).getFolderData().getPath()).toFile());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    },
    OPEN_DOWNLOAD_SITE {
        @Override
        public void execute(MusicSelector selector) {
            Bar current = selector.getBarRender().getSelected();
            if(current instanceof SongBar) {
            	final SongData song = ((SongBar) current).getSongData();
				if (song != null && song.getUrl() != null) {
					try {
						URI uri = new URI(song.getUrl());
						Desktop.getDesktop().browse(uri);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				if (song != null && song.getAppendurl() != null) {
					try {
						URI uri = new URI(song.getAppendurl());
						Desktop.getDesktop().browse(uri);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
            }
        }
    },
    OPEN_DOCUMENT {
        @Override
        public void execute(MusicSelector selector) {
            if (!Desktop.isDesktopSupported()) {
            	return;
            }
            Bar current = selector.getBarRender().getSelected();
            if(current instanceof SongBar && ((SongBar) current).existsSong()) {
    			try (DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(((SongBar) current).getSongData().getPath()).getParent())) {
    				paths.forEach(p -> {
    					if(!Files.isDirectory(p) && p.toString().toLowerCase().endsWith(".txt")) {
                            try {
								Desktop.getDesktop().open(p.toFile());
							} catch (IOException e) {
								e.printStackTrace();
							}
    					}
    				});
    			} catch (Throwable e) {
    				e.printStackTrace();
    			}
            }
        }
    },
    UPDATE_FOLDER {

		@Override
		public void execute(MusicSelector selector) {
            Bar selected = selector.getBarRender().getSelected();
			if(selected instanceof FolderBar) {
				selector.main.updateSong(((FolderBar) selected).getFolderData().getPath());
			} else if(selected instanceof TableBar) {
				selector.main.updateTable((TableBar) selected);
			}
		}
    },
    JUDGETIMING_UP {
		@Override
		public void execute(MusicSelector selector) {
	        final PlayerConfig config = selector.main.getPlayerConfig();
            if (config.getJudgetiming() < 99) {
                config.setJudgetiming(config.getJudgetiming() + 1);
                selector.play(SOUND_CHANGEOPTION);
            }
		}    	
    },
    JUDGETIMING_DOWN {
		@Override
		public void execute(MusicSelector selector) {
	        final PlayerConfig config = selector.main.getPlayerConfig();
            if (config.getJudgetiming() > -99) {
                config.setJudgetiming(config.getJudgetiming() - 1);
                selector.play(SOUND_CHANGEOPTION);
            }
		}    	
    },
    DURATION_UP {
		@Override
		public void execute(MusicSelector selector) {
            Bar current = selector.getBarRender().getSelected();
            PlayConfig pc = null;
            if (current instanceof SongBar && ((SongBar)current).existsSong()) {
                SongBar song = (SongBar) current;
                pc = selector.main.getPlayerConfig().getPlayConfig(song.getSongData().getMode());
            }
            if (pc != null && pc.getDuration() < 2000) {
                pc.setDuration(pc.getDuration() + 1);
                selector.play(SOUND_CHANGEOPTION);
            }
		}    	
    },
    DURATION_DOWN {
		@Override
		public void execute(MusicSelector selector) {
            Bar current = selector.getBarRender().getSelected();
            PlayConfig pc = null;
            if (current instanceof SongBar && ((SongBar)current).existsSong()) {
                SongBar song = (SongBar) current;
                pc = selector.main.getPlayerConfig().getPlayConfig(song.getSongData().getMode());
            }
            if (pc != null && pc.getDuration() > 1) {
                pc.setDuration(pc.getDuration() - 1);
                selector.play(SOUND_CHANGEOPTION);
            }
		}    	
    },
    CHANGE_BGA_SHOW {
		@Override
		public void execute(MusicSelector selector) {
            selector.main.getConfig().setBga((selector.main.getConfig().getBga() + 1) % 3);
            selector.play(SOUND_CHANGEOPTION);
		}    	
    }
    ;

    public abstract void execute(MusicSelector selector);
}
