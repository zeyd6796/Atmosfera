/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.hephaestus.atmosfera.client.sound;

import dev.hephaestus.atmosfera.Atmosfera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class AtmosphericSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
	private final AtmosphericSoundDefinition definition;

	private int volumeTransitionTimer = 0;
	private boolean done = false;

	public AtmosphericSoundInstance(AtmosphericSoundDefinition definition, float volume) {
		super(definition.getSoundEvent(), SoundCategory.AMBIENT);
		this.definition = definition;
		this.volume = volume;
		this.done = false;
		this.repeat = true;
		this.repeatDelay = 0;
		this.field_18935 = true;
		this.looping = true; // `SubtitlesHud.render` throws a `ConcurrentModificationException` because of the off-thread processing.
	}

	@Override
	public boolean isDone() {
		return this.done;
	}

	public void markDone() {
		this.done = true;
		this.repeat = false;
	}

	@Override
	public void tick() {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client != null && client.player != null /* && !client.player.removed */ && this.volumeTransitionTimer >= 0) {
			// For the MC 1.15.2 legacy support:
//			this.x = (float) client.player.getX();
			// For the MC 1.14.4 legacy support:
//			this.x = (float) client.player.getPos().x;
			this.x = client.player.getX();
			this.y = client.player.getY();
			this.z = client.player.getZ();

			float volume = this.definition.getVolume();
			if (volume >= this.volume + 0.0125) {
				++this.volumeTransitionTimer;
			} else if (volume < this.volume - 0.0125 || this.volumeTransitionTimer == 0) { // Completes the transition by not getting stuck at zero.
				this.volumeTransitionTimer -= 1;
			}

			this.volumeTransitionTimer = Math.min(this.volumeTransitionTimer, 60); // 80 does not get fully completed.
			this.volume = MathHelper.clamp(this.volumeTransitionTimer / 60.0F, 0.0F, 1.0F);
			this.pitch = this.definition.getPitch();

			// Only for testing.
//			Atmosfera.LOG.info("[Atmosfera] id: " + this.definition.getId()
//					+ " - volume: " + volume
//					+ " - this.volume: " + this.volume
//					+ " - volumeTransitionTimer: " + this.volumeTransitionTimer);
		} else {
			this.markDone();
//			this.done = true;
//			Atmosfera.LOG.info("[Atmosfera] done: " + this.definition.getId()); // Only for testing.
		}
	}
}
