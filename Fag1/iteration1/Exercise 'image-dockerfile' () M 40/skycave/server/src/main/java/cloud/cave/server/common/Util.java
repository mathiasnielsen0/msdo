/*
 * Copyright (C) 2015 - 2023. Henrik Bærbak Christensen, Aarhus University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package cloud.cave.server.common;

import cloud.cave.service.wall.MessageRecord;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** Various utility methods, notably for handling time formatting
 *
 */
public class Util {
  /** Compute an 'ago' string that shows how many minutes, hours, etc.
   * ago some time instance happened.
   * @param someTimeAgo the time when something happened
   * @return a string formatting it is as 'X units ago'.
   */
  public static String calcSinceNow(ZonedDateTime someTimeAgo) {
    ZonedDateTime now = ZonedDateTime.now();
    Period period = Period.between(someTimeAgo.toLocalDate(), now.toLocalDate());
    Duration duration = Duration.between(someTimeAgo, now);

    long cMin = duration.toMinutes();

    if (cMin == 0) return "just now";
    if (cMin < 60) return cMin + " minutes ago";

    long cHour = duration.toHours();

    if (cHour < 24) return cHour + " hours ago";

    // Special handling of daylight saving:
    long cDays = duration.toDays();
    if (cDays < 30) {
      // FIXME: This algorithm does not work the first days of March
      // as February is only 28/29 days.

      // cDays will be 13 for an instant 14 days prior
      // if that instant is in winter time, and now is
      // summer time, therefore we pick the period instead
      return period.getDays() + " days ago";
    }
    long cYears = period.getYears();
    if (cYears > 0) return cYears + " years ago";

    long cMonths = period.getMonths();
    if (cMonths < 12) return cMonths + " months ago";

    return "some time ago";
  }

  public static String calcSinceNow(String timeStampISO8601) {
    ZonedDateTime dt =
            ZonedDateTime.parse(timeStampISO8601,
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    return calcSinceNow(dt);
  }

  public static String formatWallPosting(MessageRecord m) {
    return "[" + m.getCreatorName() + ", " + calcSinceNow(m.getCreatorTimeStampISO8601()) +  "] " + m.getContents();
  }

}
