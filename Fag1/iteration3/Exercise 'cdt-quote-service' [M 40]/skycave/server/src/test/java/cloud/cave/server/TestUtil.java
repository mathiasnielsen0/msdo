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

package cloud.cave.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import cloud.cave.server.common.Util;
import cloud.cave.service.wall.MessageRecord;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


/** TDD and testing of various utility function.
 *
 */
public class TestUtil {

  @Test
  public void shouldTDDTimeUtil() {
    ZonedDateTime t1 = ZonedDateTime.now(), t2;
    assertThat(Util.calcSinceNow(t1), is("just now"));

    t2 = t1.minusMinutes(8);
    assertThat(Util.calcSinceNow(t2), is("8 minutes ago"));

    t2 = t1.minusMinutes(59);
    assertThat(Util.calcSinceNow(t2), is("59 minutes ago"));

    t2 = t1.minusMinutes(60);
    assertThat(Util.calcSinceNow(t2), is("1 hours ago"));
    t2 = t1.minusHours(3);
    assertThat(Util.calcSinceNow(t2), is("3 hours ago"));
    t2 = t1.minusHours(23);
    assertThat(Util.calcSinceNow(t2), is("23 hours ago"));

    t2 = t1.minusHours(28);
    assertThat(Util.calcSinceNow(t2), is("1 days ago"));

    t2 = t1.minusDays(14);
    assertThat(Util.calcSinceNow(t2), is("14 days ago"));
    t2 = t1.minusDays(27);
    assertThat(Util.calcSinceNow(t2), is("27 days ago"));

    t2 = t1.minusDays(31);
    assertThat(Util.calcSinceNow(t2), is("1 months ago"));

    t2 = t1.minusMonths(3);
    assertThat(Util.calcSinceNow(t2), is("3 months ago"));
    t2 = t1.minusMonths(11);
    assertThat(Util.calcSinceNow(t2), is("11 months ago"));

    t2 = t1.minusYears(12);
    assertThat(Util.calcSinceNow(t2), is("12 years ago"));
  }

  @Test
  public void shouldHandleISO8601Strings() {
    // A bit self-defeating test which is almost
    // longer than production code
    ZonedDateTime t1 = ZonedDateTime.now();
    String t1AsString = t1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    assertThat(Util.calcSinceNow(t1AsString), is("just now"));
  }

  @Test
  public void shouldFormatWallPosting() {
    MessageRecord mr = new MessageRecord("Fisk er godt", "7", "Hans");
    mr.setCreatorTimeStampISO8601(ZonedDateTime.now().minusMinutes(17));
    assertThat(Util.formatWallPosting(mr), is("[Hans, 17 minutes ago] Fisk er godt"));
  }
}