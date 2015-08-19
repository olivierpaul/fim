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
package org.fim.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.fim.model.CorruptedStateException;
import org.fim.model.FileState;
import org.fim.model.Parameters;
import org.fim.model.State;
import org.fim.util.Logger;

public class StateManager
{
	public static final String STATE_EXTENSION = ".json.gz";

	private final Parameters parameters;

	public StateManager(Parameters parameters)
	{
		this.parameters = parameters;
	}

	public void createNewState(State state) throws IOException
	{
		int lastStateNumber = getLastStateNumber();
		lastStateNumber++;
		state.saveToGZipFile(getStateFile(lastStateNumber));
		saveLastStateNumber(lastStateNumber);
	}

	public State loadLastState() throws IOException
	{
		int lastStateNumber = getLastStateNumber();
		return loadState(lastStateNumber);
	}

	public State loadState(int stateNumber) throws IOException
	{
		Path stateFile = getStateFile(stateNumber);
		if (!Files.exists(stateFile))
		{
			throw new IllegalStateException(String.format("Unable to load State file %d from directory %s", stateNumber, parameters.getRepositoryStatesDir()));
		}

		try
		{
			State state = State.loadFromGZipFile(stateFile);

			adjustAccordingToHashMode(state);

			return state;
		}
		catch (CorruptedStateException e)
		{
			throw new IllegalStateException(String.format("The content of the State file #%d have been modified and may be corrupted", stateNumber));
		}
	}

	private void adjustAccordingToHashMode(State state)
	{
		// Replace by 'no_hash' accurately to be able to compare the FileState entry
		switch (parameters.getHashMode())
		{
			case dontHashFiles:
				for (FileState fileState : state.getFileStates())
				{
					fileState.getFileHash().setSmallBlockHash(FileState.NO_HASH);
					fileState.getFileHash().setMediumBlockHash(FileState.NO_HASH);
					fileState.getFileHash().setFullHash(FileState.NO_HASH);
				}
				break;

			case hashSmallBlock:
				for (FileState fileState : state.getFileStates())
				{
					fileState.getFileHash().setMediumBlockHash(FileState.NO_HASH);
					fileState.getFileHash().setFullHash(FileState.NO_HASH);
				}
				break;

			case hashMediumBlock:
				for (FileState fileState : state.getFileStates())
				{
					fileState.getFileHash().setFullHash(FileState.NO_HASH);
				}
				break;

			case computeAllHash:
				// Nothing to do
				break;
		}
	}

	/**
	 * @return the State file formatted like this: <statesDir>/state_<stateNumber>.json.gz
	 */
	public Path getStateFile(int stateNumber)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("state_").append(stateNumber).append(STATE_EXTENSION);
		return parameters.getRepositoryStatesDir().resolve(builder.toString());
	}

	public int getLastStateNumber()
	{
		Path stateFile;
		int number;
		boolean lastStateFileDesynchronized = false;

		SettingsManager settingsManager = new SettingsManager(parameters);
		if (settingsManager.isSaved())
		{
			number = settingsManager.getLastStateNumber();
			stateFile = getStateFile(number);
			if (Files.exists(stateFile))
			{
				return number;
			}

			if (number > 0)
			{
				lastStateFileDesynchronized = true;
			}
		}

		for (int index = 1; ; index++)
		{
			stateFile = getStateFile(index);
			if (!Files.exists(stateFile))
			{
				number = index - 1;
				if (lastStateFileDesynchronized)
				{
					Logger.error(String.format("lastStateNumber desynchronized. Resetting it to %d.", number));
					saveLastStateNumber(number);
				}
				return number;
			}
		}
	}

	private void saveLastStateNumber(int lastStateNumber)
	{
		if (lastStateNumber != -1)
		{
			SettingsManager settingsManager = new SettingsManager(parameters);
			settingsManager.setLastStateNumber(lastStateNumber);
			settingsManager.save();
		}
	}
}
