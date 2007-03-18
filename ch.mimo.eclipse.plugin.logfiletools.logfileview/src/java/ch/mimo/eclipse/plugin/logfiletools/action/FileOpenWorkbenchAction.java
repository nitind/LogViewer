package ch.mimo.eclipse.plugin.logfiletools.action;

import ch.mimo.eclipse.plugin.logfiletools.action.delegate.FileOpenActionDelegate;

/*
 * Copyright (c) 2007 - 2011 by Michael Mimo Moratti
 * Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

public class FileOpenWorkbenchAction extends AbstractWorkbenchAction {
	
	// Constructor ------------------------------------------------------------------
	
	public FileOpenWorkbenchAction() {
		super(new FileOpenActionDelegate());
	}
}
