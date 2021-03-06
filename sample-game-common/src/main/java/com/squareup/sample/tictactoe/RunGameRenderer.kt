/*
 * Copyright 2017 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.sample.tictactoe

import com.squareup.sample.tictactoe.RunGameState.MaybeQuitting
import com.squareup.sample.tictactoe.RunGameState.Playing
import com.squareup.viewbuilder.EventHandlingScreen.Companion.ignoreEvents
import com.squareup.viewbuilder.MainAndModalScreen
import com.squareup.workflow.Renderer
import com.squareup.workflow.WorkflowInput
import com.squareup.workflow.WorkflowPool

object RunGameRenderer : Renderer<RunGameState, RunGameEvent, MainAndModalScreen<*, *>> {

  override fun render(
    state: RunGameState,
    workflow: WorkflowInput<RunGameEvent>,
    workflows: WorkflowPool
  ): MainAndModalScreen<*, *> {
    return when (state) {

      is Playing -> {
        return TakeTurnsRenderer
            .render(state.delegateState, workflows.input(state.id), workflows)
            .let { MainAndModalScreen<Any, Any>(it) }
      }

      is RunGameState.NewGame -> MainAndModalScreen<Any, Any>(NewGameScreen(workflow::sendEvent))

      is MaybeQuitting -> MainAndModalScreen(
          GamePlayScreen(state.completedGame.lastTurn, ignoreEvents()),
          ConfirmQuitScreen(workflow::sendEvent)
      )

      is RunGameState.GameOver -> MainAndModalScreen<Any, Any>(
          GameOverScreen(state, workflow::sendEvent)
      )
    }
  }
}
