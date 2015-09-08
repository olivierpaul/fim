/*
 * This file is part of Fim - File Integrity Manager
 *
 * Copyright (C) 2015  Etienne Vrignaud
 *
 * Fim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Fim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Fim.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fim.command;

import org.fim.internal.StateComparator;
import org.fim.internal.StateGenerator;
import org.fim.internal.StateManager;
import org.fim.model.Context;
import org.fim.model.State;

public class DiffCommand extends AbstractCommand
{
	@Override
	public String getCmdName()
	{
		return "diff";
	}

	@Override
	public String getShortCmdName()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "Compare the current directory State with the previous one. You can get a quick result by using the -m or -k or -f";
	}

	@Override
	public void execute(Context context) throws Exception
	{
		checkHashMode(context, Option.ALLOW_COMPATIBLE);

		State currentState = new StateGenerator(context).generateState("", context.getRepositoryRootDir(), CURRENT_DIRECTORY);
		State lastState = new StateManager(context).loadLastState();

		if (context.isInvokedFromSubDirectory())
		{
			lastState.filterDirectory(context.getRepositoryRootDir(), CURRENT_DIRECTORY, true);
		}

		new StateComparator(context).compare(lastState, currentState).displayChanges();
	}
}
