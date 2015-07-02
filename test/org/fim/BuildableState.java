/*
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.fim.model.FileState;
import org.fim.model.State;

/**
 * @author evrignaud
 */
public class BuildableState extends State
{
	public BuildableState()
	{
		setFileStates(new ArrayList<FileState>());
	}

	public BuildableState addFiles(String... fileNames)
	{
		BuildableState newState = cloneState();
		for (String fileName : fileNames)
		{
			if (findFileState(newState, fileName, false) != null)
			{
				throw new IllegalArgumentException("New file: Duplicate fileName " + fileName);
			}

			// By default put the fileName as fileContent that will be the hash two
			FileState fileState = new FileState(fileName, getNow(), fileName);
			newState.getFileStates().add(fileState);
		}
		Collections.sort(newState.getFileStates());
		return newState;
	}

	public BuildableState copy(String sourceFileName, String targetFileName)
	{
		BuildableState newState = cloneState();
		if (findFileState(newState, targetFileName, false) != null)
		{
			throw new IllegalArgumentException("Copy: File already exist " + targetFileName);
		}

		FileState sourceFileState = findFileState(newState, sourceFileName, true);
		FileState targetFileState = new FileState(targetFileName, sourceFileState.getLastModified(), sourceFileState.getHash());
		newState.getFileStates().add(targetFileState);
		Collections.sort(newState.getFileStates());
		return newState;
	}

	public BuildableState rename(String sourceFileName, String targetFileName)
	{
		BuildableState newState = cloneState();
		if (findFileState(newState, targetFileName, false) != null)
		{
			throw new IllegalArgumentException("Rename: File already exist " + targetFileName);
		}

		FileState fileState = findFileState(newState, sourceFileName, true);
		fileState.setFileName(targetFileName);
		Collections.sort(newState.getFileStates());
		return newState;
	}

	public BuildableState delete(String fileName)
	{
		BuildableState newState = cloneState();
		FileState fileState = findFileState(newState, fileName, true);
		newState.getFileStates().remove(fileState);
		return newState;
	}

	public BuildableState touch(String fileName)
	{
		BuildableState newState = cloneState();
		FileState fileState = findFileState(newState, fileName, true);
		long now = getNow();
		if (now <= fileState.getLastModified())
		{
			now = fileState.getLastModified() + 1;
		}
		fileState.setLastModified(now);
		return newState;
	}

	public BuildableState setContent(String fileName, String fileContent)
	{
		BuildableState newState = cloneState();
		FileState fileState = findFileState(newState, fileName, true);
		fileState.setHash(fileContent);
		return newState;
	}

	public BuildableState appendContent(String fileName, String fileContent)
	{
		BuildableState newState = cloneState();
		FileState fileState = findFileState(newState, fileName, true);
		fileState.setHash(fileState.getHash() + fileContent);
		return newState;
	}

	private FileState findFileState(BuildableState state, String fileName, boolean throwEx)
	{
		for (FileState fileState : state.getFileStates())
		{
			if (fileState.getFileName().equals(fileName))
			{
				return fileState;
			}
		}

		if (throwEx)
		{
			throw new IllegalArgumentException("Unknown file " + fileName);
		}
		return null;
	}

	private long getNow()
	{
		return new Date().getTime();
	}

	private BuildableState cloneState()
	{
		BuildableState newState = new BuildableState();
		newState.setMessage(getMessage());

		ArrayList<FileState> newFileStates = new ArrayList<>();
		for (FileState fileState : getFileStates())
		{
			FileState newFileState = new FileState(fileState.getFileName(), fileState.getLastModified(), fileState.getHash());
			newFileStates.add(newFileState);
		}
		newState.setFileStates(newFileStates);

		return newState;
	}

}
