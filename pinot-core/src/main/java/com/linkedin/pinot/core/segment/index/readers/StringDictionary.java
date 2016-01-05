/**
 * Copyright (C) 2014-2015 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.pinot.core.segment.index.readers;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.linkedin.pinot.common.segment.ReadMode;
import static com.linkedin.pinot.core.segment.creator.impl.V1Constants.Str.STRING_PAD_CHAR;
import com.linkedin.pinot.core.segment.index.ColumnMetadata;

/**
 * Nov 14, 2014
 */

public class StringDictionary extends ImmutableDictionaryReader {
  private final int lengthofMaxEntry;
  private static Charset UTF_8 = Charset.forName("UTF-8");

  public StringDictionary(File dictFile, ColumnMetadata metadata, ReadMode mode)
      throws IOException {
    super(dictFile, metadata.getCardinality(), metadata.getStringColumnMaxLength(),
        mode == ReadMode.mmap);
    lengthofMaxEntry = metadata.getStringColumnMaxLength();
  }

  @Override
  public int indexOf(Object rawValue) {
    final String lookup = rawValue.toString();
    final int differenceInLength = lengthofMaxEntry - lookup.length();
    final StringBuilder bld = new StringBuilder();
    bld.append(lookup);
    for (int i = 0; i < differenceInLength; i++) {
      bld.append(STRING_PAD_CHAR);
    }
    return stringIndexOf(bld.toString());
  }

  @Override
  public String get(int dictionaryId) {
    if ((dictionaryId == -1) || (dictionaryId >= length())) {
      return "null";
    }
    String val = getString(dictionaryId);
    byte[] bytes = val.getBytes(UTF_8);
    for (int i = 0; i < lengthofMaxEntry; i++) {
      if (bytes[i] == STRING_PAD_CHAR) {
        return new String(bytes, 0, i, UTF_8);
      }
    }
    return val;
  }

  @Override
  public long getLongValue(int dictionaryId) {
    throw new RuntimeException("cannot converted string to long");
  }

  @Override
  public double getDoubleValue(int dictionaryId) {
    throw new RuntimeException("cannot converted string to double");
  }

  @Override
  public String getStringValue(int dictionaryId) {
    return getString(dictionaryId);
  }

  @Override
  public String toString(int dictionaryId) {
    return get(dictionaryId);
  }

  private String getString(int dictionaryId) {
    return dataFileReader.getString(dictionaryId, 0);
  }
}
