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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.fim.model.Difference;

/**
 * @author evrignaud
 */
public class StateAssert
{
	protected void assertNothingModified(StateComparator cmp)
	{
		assertThat(cmp.somethingModified()).isFalse();
	}

	protected void assertOnlyFilesAdded(StateComparator cmp, String... fileNames)
	{
		assertGotOnlyModifications(cmp, Modification.ADDED);
		assertFilesModified(cmp, Modification.ADDED, fileNames);
	}

	protected void assertOnlyDatesModified(StateComparator cmp, String... fileNames)
	{
		assertGotOnlyModifications(cmp, Modification.DATE_MODIFIED);
		assertFilesModified(cmp, Modification.DATE_MODIFIED, fileNames);
	}

	protected void assertOnlyContentModified(StateComparator cmp, String... fileNames)
	{
		assertGotOnlyModifications(cmp, Modification.CONTENT_MODIFIED);
		assertFilesModified(cmp, Modification.CONTENT_MODIFIED, fileNames);
	}

	protected void assertOnlyFileRenamed(StateComparator cmp, FileNameDiff... fileNameDiffs)
	{
		assertGotOnlyModifications(cmp, Modification.RENAMED);
		assertFilesModified(cmp, Modification.RENAMED, fileNameDiffs);
	}

	protected void assertOnlyFileDuplicated(StateComparator cmp, FileNameDiff... fileNameDiffs)
	{
		assertGotOnlyModifications(cmp, Modification.DUPLICATED);
		assertFilesModified(cmp, Modification.DUPLICATED, fileNameDiffs);
	}

	protected void assertOnlyFileDeleted(StateComparator cmp, String... fileNames)
	{
		assertGotOnlyModifications(cmp, Modification.DELETED);
		assertFilesModified(cmp, Modification.DELETED, fileNames);
	}

	protected void assertGotOnlyModifications(StateComparator cmp, Modification... modifications)
	{
		List<Modification> modificationsList = Arrays.asList(modifications);

		for (Modification modification : Modification.values())
		{
			if (modificationsList.contains(modification) == false)
			{
				List<Difference> differences = getDifferences(cmp, modification);
				assertThat(differences.isEmpty()).isTrue();
			}
		}
	}

	protected void assertFilesModified(StateComparator cmp, Modification modification, String... fileNames)
	{
		List<String> fileNamesList = Arrays.asList(fileNames);

		List<Difference> differences = getDifferences(cmp, modification);
		assertThat(fileNamesList.size()).isEqualTo(differences.size());

		for (Difference difference : differences)
		{
			assertThat(fileNamesList.contains(difference.getFileState().getFileName())).isTrue();
		}
	}

	protected void assertFilesModified(StateComparator cmp, Modification modification, FileNameDiff... fileNameDiffs)
	{
		List<FileNameDiff> fileNameDiffsList = Arrays.asList(fileNameDiffs);

		List<Difference> differences = getDifferences(cmp, modification);
		assertThat(fileNameDiffsList.size()).isEqualTo(differences.size());

		for (Difference difference : differences)
		{
			assertThat(fileNameDiffsList.contains(new FileNameDiff(difference))).isTrue();
		}
	}

	private List<Difference> getDifferences(StateComparator cmp, Modification modification)
	{
		switch (modification)
		{
			case ADDED:
				return cmp.getAdded();

			case DELETED:
				return cmp.getDeleted();

			case DATE_MODIFIED:
				return cmp.getDateModified();

			case CONTENT_MODIFIED:
				return cmp.getContentModified();

			case DUPLICATED:
				return cmp.getDuplicated();

			case RENAMED:
				return cmp.getRenamed();
		}

		throw new IllegalArgumentException("Invalid diffKind " + modification);
	}

}
