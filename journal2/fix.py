import os
def fix_fxml_files():
    folder_path = ".\\src\\main\\resources\\fxml\\"
    fxml_files = [f for f in os.listdir(folder_path) if f.endswith('.fxml')]    
    old_xmlns = 'xmlns="http://javafx.com/javafx/23.0.1"'
    new_xmlns = 'xmlns="http://javafx.com/javafx/21"'

    no_occurrences = 0
    one_occurrence = 0
    multiple_occurrences = 0
    verified = 0
    print(f"\nFound {len(fxml_files)} fxml files.")

    for fxml_file in fxml_files:
        # Read file content
        with open(folder_path + fxml_file, 'r', encoding='utf-8') as f:
            content = f.read()
            occurrences = content.count(old_xmlns)

        if new_xmlns in content:
            verified += 1
            continue

        if occurrences == 0:
            print(f"Info: No xmlns occurrences in {fxml_file}. No changes made.")
            no_occurrences += 1
        elif occurrences == 1:
            new_content = content.replace(old_xmlns, new_xmlns)
    
            with open(folder_path + fxml_file, 'w', encoding='utf-8') as f:
                f.write(new_content)
                print(f"Updated {fxml_file}")
            one_occurrence += 1
        else:
            print(f"Warning: Multiple xmlns occurrences in {fxml_file}. No changes made.")
            multiple_occurrences += 1

    print(f"\nSummary:")

    if len(fxml_files) == verified:
        # Write in green
        print(f"\033[32m  Total fxml files: {len(fxml_files)}, verified: {verified}, updated: {one_occurrence}\033[0m")
    else:
        # Write in red
        print(f"\033[31m  Total fxml files: {len(fxml_files)}, verified: {verified}, updated: {one_occurrence}\033[0m")
        
    print(f"  No occurrences: {no_occurrences}")
    print(f"  One occurrence: {one_occurrence}")
    print(f"  Multiple occurrences: {multiple_occurrences}")

if __name__ == "__main__":
    fix_fxml_files()














