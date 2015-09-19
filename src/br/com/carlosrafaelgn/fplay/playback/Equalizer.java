//
// FPlayAndroid is distributed under the FreeBSD License
//
// Copyright (c) 2013-2014, Carlos Rafael Gimenes das Neves
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// The views and conclusions contained in the software and documentation are those
// of the authors and should not be interpreted as representing official policies,
// either expressed or implied, of the FreeBSD Project.
//
// https://github.com/carlosrafaelgn/FPlayAndroid
//
package br.com.carlosrafaelgn.fplay.playback;

import java.util.Arrays;

import br.com.carlosrafaelgn.fplay.util.SerializableMap;

public final class Equalizer {
	private static int sessionId = Integer.MIN_VALUE, minBandLevel, maxBandLevel, preset;
	private static boolean enabled;
	private static int[] bandLevels, bandFrequencies;
	private static android.media.audiofx.Equalizer theEqualizer;
	
	public static void deserializePreset(SerializableMap opts) {
		preset = -1;
		int count = opts.getInt(Player.OPT_EQUALIZER_LEVELCOUNT);
		if (count > 0) {
			if (bandLevels == null) {
				if (count > 512)
					count = 512;
				bandLevels = new int[count];
			}
			for (int i = bandLevels.length - 1; i >= 0; i--)
				bandLevels[i] = opts.getInt(Player.OPT_EQUALIZER_LEVEL0 + i, bandLevels[i]);
		}		
	}

	public static void serializePreset(SerializableMap opts) {
		opts.put(Player.OPT_EQUALIZER_LEVELCOUNT, (bandLevels != null) ? bandLevels.length : 0);
		if (bandLevels != null) {
			for (int i = bandLevels.length - 1; i >= 0; i--)
				opts.put(Player.OPT_EQUALIZER_LEVEL0 + i, bandLevels[i]);
		}
	}

	static void loadConfig(SerializableMap opts) {
		enabled = (opts.hasBits() ? opts.getBit(Player.OPTBIT_EQUALIZER_ENABLED) : opts.getBoolean(Player.OPT_EQUALIZER_ENABLED));
		deserializePreset(opts);
		preset = opts.getInt(Player.OPT_EQUALIZER_PRESET, -1);
	}

	static void saveConfig(SerializableMap opts) {
		opts.putBit(Player.OPTBIT_EQUALIZER_ENABLED, enabled);
		opts.put(Player.OPT_EQUALIZER_PRESET, preset);
		serializePreset(opts);
	}
	
	static void initialize(int newSessionId) {
		if (newSessionId != Integer.MIN_VALUE)
			sessionId = newSessionId;
		try {
			theEqualizer = new android.media.audiofx.Equalizer(0, sessionId);
		} catch (Throwable ex) {
			ex.printStackTrace();
			return;
		}
		final int bandCount = theEqualizer.getNumberOfBands();
		if (bandLevels == null)
			bandLevels = new int[bandCount];
		else if (bandLevels.length != bandCount)
			bandLevels = Arrays.copyOf(bandLevels, bandCount);
		boolean copyFrequencies = false;
		if (bandFrequencies == null) {
			bandFrequencies = new int[bandCount];
			copyFrequencies = true;
		} else if (bandFrequencies.length != bandCount) {
			bandFrequencies = Arrays.copyOf(bandFrequencies, bandCount);
			copyFrequencies = true;
		}
		if (copyFrequencies) {
			for (int i = bandCount - 1; i >= 0; i--)
				bandFrequencies[i] = theEqualizer.getCenterFreq((short)i) / 1000;
		}
		short[] l = theEqualizer.getBandLevelRange();
		if (l == null || l.length != 2) {
			minBandLevel = -1500;
			maxBandLevel = 1500;
		} else {
			minBandLevel = (int)l[0];
			maxBandLevel = (int)l[1];
		}
	}
	
	static void release() {
		if (theEqualizer != null) {
			try {
				theEqualizer.release();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			theEqualizer = null;
		}
	}
	
	/*public static boolean isUsingFactoryPreset() {
		return (preset >= 0);
	}
	
	public static int getFactoryPresetCount() {
		return ((theEqualizer == null) ? 0 : (int)theEqualizer.getNumberOfPresets());
	}
	
	public static String getFactoryPresetName(int preset) {
		return ((theEqualizer == null || preset < 0 || preset >= (int)theEqualizer.getNumberOfPresets()) ? null : theEqualizer.getPresetName((short)preset));
	}
	
	public static int getCurrentFactoryPreset() {
		return preset;
	}
	
	public static void setCurrentFactoryPreset(int preset) {
		if (theEqualizer == null)
			return;
		if (preset >= 0 && preset < theEqualizer.getNumberOfPresets()) {
			Equalizer.preset = preset;
			try {
				theEqualizer.usePreset((short)preset);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		} else if (preset < 0) {
			Equalizer.preset = -1;
			applyAllBandSettings();
		}
	}*/
	
	public static boolean isSupported() {
		return ((bandLevels != null) && (bandLevels.length > 0));
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	static void setEnabled(boolean enabled) {
		Equalizer.enabled = enabled;
		if (theEqualizer != null) {
			try {
				if (!enabled) {
					theEqualizer.setEnabled(false);
				} else if (sessionId != Integer.MIN_VALUE) {
					applyAllBandSettings();
					theEqualizer.setEnabled(true);
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			Equalizer.enabled = theEqualizer.getEnabled();
		}
	}
	
	public static int getBandCount() {
		return ((bandLevels == null) ? 0 : bandLevels.length);
	}
	
	public static int getBandFrequency(int band) {
		return ((bandFrequencies == null || band >= bandFrequencies.length) ? 0 : bandFrequencies[band]);
	}
	
	public static int getMinBandLevel() {
		return minBandLevel;
	}
	
	public static int getMaxBandLevel() {
		return maxBandLevel;
	}
	
	public static int getBandLevel(int band) {
		return ((bandLevels == null || band >= bandLevels.length) ? 0 : bandLevels[band]);
	}
	
	public static void setBandLevel(int band, int level) {
		if (bandLevels == null || band >= bandLevels.length)
			return;
		if (level > maxBandLevel)
			level = maxBandLevel;
		else if (level < minBandLevel)
			level = minBandLevel;
		bandLevels[band] = level;
	}

	static void commit(int band) {
		if (preset >= 0) {
			preset = -1;
			applyAllBandSettings();
		} else if (band < 0) {
			applyAllBandSettings();
		} else if (theEqualizer != null) {
			try {
				theEqualizer.setBandLevel((short)band, (short)bandLevels[band]);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private static void applyAllBandSettings() {
		if (theEqualizer == null)
			return;
		if (preset >= 0 && preset < theEqualizer.getNumberOfPresets()) {
			try {
				theEqualizer.usePreset((short)preset);
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
			return;
		}
		if (bandLevels != null && bandLevels.length > 0) {
			preset = -1;
			try {
				final android.media.audiofx.Equalizer.Settings s = new android.media.audiofx.Equalizer.Settings();
				int i = ((bandLevels.length > (int)theEqualizer.getNumberOfBands()) ? (int)theEqualizer.getNumberOfBands() : bandLevels.length);
				s.bandLevels = new short[i];
				s.curPreset = -1;
				s.numBands = (short)(i);
				for (i = i - 1; i >= 0; i--) {
					int level = bandLevels[i];
					if (level > maxBandLevel) {
						level = maxBandLevel;
						bandLevels[i] = level;
					} else if (level < minBandLevel) {
						level = minBandLevel;
						bandLevels[i] = level;
					}
					s.bandLevels[i] = (short)level;
				}
				theEqualizer.setProperties(s);
			} catch (Throwable ex) {
				ex.printStackTrace();
				for (int i = bandLevels.length - 1; i >= 0; i--) {
					try {
						theEqualizer.setBandLevel((short)i, (short)bandLevels[i]);
					} catch (Throwable ex2) {
						ex2.printStackTrace();
					}
				}
			}
		}
	}
}
