# Prepend

## JDK

Java version 11 or higher is required.

## Arguments
The following arguments are supported:
1. `-t <path>`: sets the `target list` path (*default is "targets.txt"*).
2. `-c <path>`: sets the `copyright` path (*default is "copyright.txt"*).
3. `-r <path>`: sets the `root` path.
4. `--add <integer>`: sets the number of extra lines added after the copyright notice.
5. `--ending <"win"/"mac"/"nix">`: sets the line ending to be used for these extra lines (`\r\n`/`\r`/`\n`).

The `target list` file contains a list of target file paths (one per line; empty lines are allowed). The copyright notice (found at the `copyright` path) will be prepended to each target.

Target paths can be relative, in which case they are resolved relative to PWD. This behaviour can be overridden by passing the `-r` argument, which defines the root / base directory to be used for resolving these targets.

**!!!** Be careful with new lines. The copyright notice file is prepended as is. This means that if you want the license to be visually separate from the main body of the file, like so:
```text
LICENCE

<file contents>
```
you have to leave 2 empty lines in the license file:
```text
LICENCE ↵
↵
.
```
Alternatively, you can use the `--add` / `--ending` arguments.

## Encoding

All files are treated as byte sequences. This means that if you want to change the encoding of the licence file, you have to save it with the appropriate encoding. This program will not tamper with it.
