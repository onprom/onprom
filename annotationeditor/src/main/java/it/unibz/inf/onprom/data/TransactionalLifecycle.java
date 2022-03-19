/*
 * annotationeditor
 *
 * TransactionalLifecycle.java
 *
 * Copyright (C) 2016-2022 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.unibz.inf.onprom.data;

/**
 * All states in transactional life-cycle from XES standard
 * <p>
 *
 * @author T. E. Kalayci on 22/11/16.
 */
public enum TransactionalLifecycle {
    COMPLETE("Complete"),
    SCHEDULE("Schedule"),
    ASSIGN("Assign"),
    REASSIGN("Reassign"),
    START("Start"),
    SUSPEND("Suspend"),
    RESUME("Resume"),
    ABORT_ACTIVITY("Abort Activity"),
    ABORT_CASE("Abort Case"),
    SUCCESSFUL_TERMINATION("Successful Termination"),
    AUTOSKIP("Auto Skip"),
    UNSUCCESSFUL_TERMINATION("Unsuccessful Termination"),
    MANUALSKIP("Manual Skip"),
    WITHDRAW("Withdraw");

    private final String state;

    TransactionalLifecycle(String _state) {
        this.state = _state;
    }

    public String toString() {
        return state;
    }
}