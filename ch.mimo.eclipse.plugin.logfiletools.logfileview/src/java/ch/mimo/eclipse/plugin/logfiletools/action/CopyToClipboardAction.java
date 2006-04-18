package ch.mimo.eclipse.plugin.logfiletools.action;

import org.eclipse.swt.widgets.Shell;

import ch.mimo.eclipse.plugin.logfiletools.LogFileView;
import ch.mimo.eclipse.plugin.logfiletools.LogFileViewPlugin;
import ch.mimo.eclipse.plugin.logfiletools.action.delegate.CopyToClipboardActionDelegate;

/*
 * Copyright (c) 2006 by Michael Mimo Moratti
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

public class CopyToClipboardAction extends AbstractViewAction {
	// Constructor ------------------------------------------------------------------
	
	public CopyToClipboardAction(LogFileView view, Shell shell) {
		super(view,shell,new CopyToClipboardActionDelegate());
	}
	
	// Public -----------------------------------------------------------------------
	
	public void init() {
		this.setText(LogFileViewPlugin.getResourceString("menu.content.copy.text")); //$NON-NLS-1$
		this.setToolTipText(LogFileViewPlugin.getResourceString("menu.content.copy.tooltip")); //$NON-NLS-1$
	}
}
