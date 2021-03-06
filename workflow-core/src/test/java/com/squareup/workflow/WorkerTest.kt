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
package com.squareup.workflow

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.CompletableDeferred
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkerTest {

  private val pool = WorkflowPool()
  private val deferred = CompletableDeferred<Unit>()
  private val worker = deferred.asWorker()

  @Test fun whenCallSucceeds() {
    val reaction = pool.workerResult(worker, Unit)
    assertFalse(reaction.isCompleted)

    deferred.complete(Unit)

    assertEquals(Unit, reaction.getCompleted())
  }

  @Test fun whenCallFails() {
    val reaction = pool.workerResult(worker, Unit)
    assertFalse(reaction.isCompleted)

    deferred.completeExceptionally(IOException("network fail"))

    val failure = reaction.getCompletionExceptionOrNull()!!
    assertTrue(failure is ReactorException)
    assertTrue(failure.cause is IOException)
  }

  @Test fun whenInternalCoroutineCancelled() {
    val reaction = pool.workerResult(worker, Unit)
    assertFalse(reaction.isCompleted)

    deferred.cancel()

    assertFailsWith<CancellationException> { reaction.getCompleted() }
  }

  @Test fun whenWorkflowCancelled() {
    val reaction = pool.workerResult(worker, Unit)
    assertFalse(reaction.isCompleted)

    pool.abandonDelegate(worker.makeId())

    assertFailsWith<CancellationException> { reaction.getCompleted() }
  }
}
