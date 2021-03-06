/*
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

package org.f1x.io.disruptor;

import com.lmax.disruptor.ExceptionHandler;

import static org.junit.Assert.fail;

public class TestExceptionHandler implements ExceptionHandler<Object> {

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        ex.printStackTrace();
        fail (ex.getMessage());
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        ex.printStackTrace();
        fail (ex.getMessage());
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        ex.printStackTrace();
        fail (ex.getMessage());
    }
}
