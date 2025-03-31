import argparse
import re
import os
from pathlib import Path
import sys

# --- Configuration ---
# Regex to find import statements (focused on Java style)
# Captures the fully qualified name (e.g., chkMVC.chModel.Checkers.BoardModel)
IMPORT_PATTERN = re.compile(r"^\s*import\s+([\w.]+);")

# Common standard library prefixes to ignore (add more as needed for your language)
IGNORE_PREFIXES = ("java.", "javax.", "org.junit.", "junit.")

# Expected source file extension
SOURCE_EXTENSION = ".java"
# --- End Configuration ---

def find_local_dependencies(target_file_path: Path, source_root_path: Path) -> set[Path]:
    """
    Scans a target file for import statements and finds corresponding local
    source files within the specified source root.

    Args:
        target_file_path: Path object for the file to scan.
        source_root_path: Path object for the project's source root directory.

    Returns:
        A set of absolute Path objects for the found local dependencies.
    """
    if not target_file_path.is_file():
        print(f"Error: Target file not found or is not a file: {target_file_path}", file=sys.stderr)
        sys.exit(1)
    if not source_root_path.is_dir():
        print(f"Error: Source root not found or is not a directory: {source_root_path}", file=sys.stderr)
        sys.exit(1)

    dependencies = set()
    target_file_abs = target_file_path.resolve()
    source_root_abs = source_root_path.resolve()
    print(f"Scanning: {target_file_path.name}")
    print(f"Source Root: {source_root_abs}")

    try:
        with open(target_file_path, 'r', encoding='utf-8') as f:
            for i, line in enumerate(f):
                match = IMPORT_PATTERN.match(line)
                if match:
                    qualified_name = match.group(1).strip()
                    print(f"  Line {i+1}: Found import: {qualified_name}")

                    # Skip standard libraries or explicitly ignored prefixes
                    if qualified_name.startswith(IGNORE_PREFIXES):
                        print(f"    - Skipping standard/ignored prefix.")
                        continue

                    # Convert qualified name to relative path fragments
                    # e.g., chkMVC.chModel.Checkers.BoardModel -> chkMVC/chModel/Checkers/BoardModel
                    path_parts = qualified_name.split('.')
                    relative_path_str = os.path.join(*path_parts) + SOURCE_EXTENSION
                    potential_dep_path_relative = Path(relative_path_str)

                    # Construct absolute path based on source root
                    potential_dep_path_abs = (source_root_abs / potential_dep_path_relative).resolve()

                    # Check if the potential dependency file exists and is a file
                    if potential_dep_path_abs.is_file():
                        # Check if it's *different* from the input file
                        if potential_dep_path_abs != target_file_abs:
                            print(f"    - Resolved to local file: {potential_dep_path_abs}")
                            dependencies.add(potential_dep_path_abs)
                        else:
                            print(f"    - Resolved to self, skipping.")
                    else:
                        print(f"    - Did not resolve to an existing file: {potential_dep_path_abs}")

    except FileNotFoundError:
        print(f"Error: Could not open target file: {target_file_path}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"An error occurred during scanning: {e}", file=sys.stderr)
        sys.exit(1)

    return dependencies

def main():
    parser = argparse.ArgumentParser(
        description="Find local project file dependencies mentioned in import statements "
                    "within a single source file."
    )
    parser.add_argument(
        "target_file",
        help="Path to the source file to scan."
    )
    parser.add_argument(
        "source_root",
        help="Path to the project's source root directory (e.g., src/main/java)."
    )
    parser.add_argument(
        "-o", "--output",
        required=True,
        help="Path to the output text file to store the list of dependencies."
    )
    parser.add_argument(
        "--abs", "--absolute-paths",
        action="store_true",
        help="Output absolute paths instead of paths relative to the source root."
    )

    args = parser.parse_args()

    target_file = Path(args.target_file)
    source_root = Path(args.source_root)
    output_file = Path(args.output)

    # Resolve source_root upfront for relative path calculation later
    try:
        source_root_abs = source_root.resolve(strict=True)
    except FileNotFoundError:
        print(f"Error: Source root directory not found: {source_root}", file=sys.stderr)
        sys.exit(1)


    found_deps_abs = find_local_dependencies(target_file, source_root)

    print(f"\nFound {len(found_deps_abs)} unique local dependencies.")

    # Write dependencies to the output file
    try:
        output_file.parent.mkdir(parents=True, exist_ok=True) # Ensure output directory exists
        with open(output_file, 'w', encoding='utf-8') as f:
            # Sort for consistent output
            sorted_deps = sorted(list(found_deps_abs))
            for dep_path_abs in sorted_deps:
                if args.abs:
                    # Write absolute path (convert to forward slashes for consistency)
                    f.write(str(dep_path_abs).replace('\\', '/') + '\n')
                else:
                    # Calculate and write path relative to the source root
                    try:
                        relative_dep = dep_path_abs.relative_to(source_root_abs)
                        f.write(str(relative_dep).replace('\\', '/') + '\n')
                    except ValueError:
                        # Should not happen if logic is correct, but fallback to absolute
                        print(f"Warning: Could not make path relative: {dep_path_abs}. Writing absolute path.", file=sys.stderr)
                        f.write(str(dep_path_abs).replace('\\', '/') + '\n')

        print(f"Dependency list written to: {output_file}")

    except IOError as e:
        print(f"Error writing to output file {output_file}: {e}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"An unexpected error occurred during output: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()