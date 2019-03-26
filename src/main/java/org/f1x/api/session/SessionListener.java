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

package org.f1x.api.session;

import org.f1x.api.message.MessageBuilder;
import org.f1x.api.message.MessageParser;
import org.f1x.api.message.fields.MsgType;

public interface SessionListener<M extends MessageParser, B extends MessageBuilder> {

    void onStatusChanged(final SessionID sessionID, final SessionStatus oldStatus, final SessionStatus newStatus);

    void onMessage(final CharSequence msgType, final M msg);

    void beforeMessageSent(final MsgType msgType, final B messageBuilder);

    void onError(final Exception ex);
}
