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

import java.util.Scanner;

import org.fim.model.Command;
import org.fim.model.CompareMode;

public abstract class AbstractCommand implements Command
{
	protected void fastCompareNotSupported(CompareMode compareMode)
	{
		if (compareMode == CompareMode.FAST)
		{
			System.out.println("Fast compare mode not supported by this command.");
			System.exit(-1);
		}
	}

	protected boolean confirmCommand(String action)
	{
		Scanner scanner = new Scanner(System.in);
		System.out.printf("Do you really want to %s (y/n)? ", action);
		String str = scanner.next();
		if (str.equalsIgnoreCase("y"))
		{
			return true;
		}
		return false;
	}
}
