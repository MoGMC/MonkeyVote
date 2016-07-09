package com.fawkes.plugin.monkeyvote;

import java.util.UUID;

public class Vote implements Runnable {

		private UUID uuid;
		private long startTime, duration;
		private boolean cancelled = false;

		public Vote(UUID people, long startTime, long duration) {

			// duration in ticks
			uuid = people;

			// ehh?
			this.duration = duration;
			this.startTime = startTime;

		}

		@Override
		public void run() {

			// if cancelled do nuffin
			if (cancelled) {
					return;

			}

			// sends them message if they are online

			MonkeyVote.removePlayer(uuid, true);

		}

		public void cancel() {
			cancelled = true;
			MonkeyVote.removePlayer(uuid, false);

		}

		public long getTicksLeft() {

			// 20 ticks in a second
			// 1000 millis in a second
			// therefore 20 ticks in 1000 millis
			// 1000/20 = 50
			// 50 millis per tick!

			return duration - ((System.currentTimeMillis() - startTime) / 50);

		}

}
