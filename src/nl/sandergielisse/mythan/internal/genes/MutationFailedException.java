/**
 * Copyright 2016 Alexander Gielisse
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.sandergielisse.mythan.internal.genes;

public class MutationFailedException extends Exception {

  private static final long serialVersionUID = 6009940212981584159L;

  public MutationFailedException() {

  }

  public MutationFailedException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public MutationFailedException(final String message) {
    super(message);
  }

  public MutationFailedException(final Throwable cause) {
    super(cause);
  }
}
