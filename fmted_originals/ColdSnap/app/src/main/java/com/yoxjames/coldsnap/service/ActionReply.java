/*
 * Copyright (c) 2017 James Yox
 *
 * This file is part of ColdSnap.
 *
 * ColdSnap is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ColdSnap is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ColdSnap.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.yoxjames.coldsnap.service;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

/**
 * Created by yoxjames on 12/2/17.
 */
@AutoValue
public abstract class ActionReply {

    public abstract boolean isSuccess();

    @Nullable
    public abstract Throwable getError();

    public static ActionReply genericSuccess() {
        return new AutoValue_ActionReply(true, null);
    }
}
