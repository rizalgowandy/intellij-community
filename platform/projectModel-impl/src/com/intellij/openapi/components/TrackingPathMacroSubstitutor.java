/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.components;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

@ApiStatus.Internal
public interface TrackingPathMacroSubstitutor extends PathMacroSubstitutor {
  @NotNull
  Set<String> getUnknownMacros(@Nullable String componentName);

  // Mutable set
  @NotNull
  Set<String> getComponents(@NotNull Collection<String> macros);

  void addUnknownMacros(@NotNull String componentName, @NotNull Collection<String> unknownMacros);

  void invalidateUnknownMacros(@NotNull Set<String> macros);

  void reset();
}
