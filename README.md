### I18n checker
Made for concrete business aims to designate translation errors with base language against newly translated columns.

### How it works?
1. Simple CSV parser
2. Build languages model with baseline language (default — first column)
3. Validate with a couple of RegExp patterns (html tags, UNITY curly tags, control symbols, unicode escaping sequences, C-like printf arguments)
4. Write all valid lines with problems marking (as a new column) back into csv output
5. Prints result in command line

### How to use it?
Pragma: in both cases pointing of output file path is optional policy. By default, program will create output file with `_validated` postfix in the same directory where you run it (you home directory usually).

#### If wanna run jvm executable (.jar file) (any operation system with JRE installed)
Open terminal/cmd, then type `java -jar i18n-checker.jar -h` to print usage helper.
Type `java -jar i18n-checker.jar -i ~/Downloads/path-to-file.csv -o ~/Desktop/path-to-output.csv`.

#### If wanna run macos executable (.kexe file) (only MacOS x64)
Open terminal/iterm, then type `~/Downloads/i18n-checker.kexe -h` to print usage helper.
Type `~/Downloads/i18n-checker.kexe -i ~/Downloads/path-to-file.csv -o ~/Desktop/path-to-output.csv`.


