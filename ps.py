import json
from pathlib import Path

# Folders to completely skip when walking the tree
EXCLUDE_DIRS = {
    ".git",
    "__pycache__",
    "node_modules",
    ".venv",
    "env",
    "dist",
    "build",
    ".idea",
    ".vscode",
}

def dir_to_dict(path: Path) -> dict:
    """Recursively converts a directory tree into a nested dictionary."""
    node = {"name": path.name, "type": "directory", "children": []}

    try:
        # Sort items so the output JSON is predictably ordered
        for item in sorted(path.iterdir()):
            if item.is_dir():
                if item.name in EXCLUDE_DIRS:
                    continue
                node["children"].append(dir_to_dict(item))
            else:
                node["children"].append({
                    "name": item.name,
                    "type": "file"
                })
    except PermissionError:
        # Handle cases where system files cannot be read
        node["children"].append({"name": "[Permission Denied]", "type": "error"})

    return node

def generate_structure_json(project_dir: str, output_file: str = "project_structure.json"):
    """Generates the JSON file from the target directory path."""
    root_path = Path(project_dir).resolve()

    if not root_path.exists():
        print(f"Error: The path '{project_dir}' does not exist.")
        return

    # Build the dictionary tree
    structure_dict = dir_to_dict(root_path)
    # Set the actual folder name as root instead of absolute system paths
    structure_dict["name"] = root_path.name

    # Save to file
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(structure_dict, f, indent=4)

    print(f"Successfully generated project structure at: {output_file}")

if __name__ == "__main__":
    # Change '.' to a specific path if you aren't running it from the project root
    generate_structure_json(project_dir=".")
