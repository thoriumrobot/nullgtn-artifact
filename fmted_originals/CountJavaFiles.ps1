# Initialize counters
$totalJavaFiles = 0
$javaFilesWithMoreThan76Lines = 0

# Recursive search for .java files
Get-ChildItem -File -Recurse -Filter *.java | ForEach-Object {
    $file = $_
    $totalJavaFiles++

    # Count lines in the file
    $lineCount = (Get-Content $file.FullName | Measure-Object -Line).Lines

    if ($lineCount -gt 76) {
        $javaFilesWithMoreThan76Lines++
        Write-Host "$($file.FullName) has $lineCount lines"
    }
}

# Display the results
Write-Host "Total number of .java files: $totalJavaFiles"
Write-Host "Number of .java files with more than 76 lines: $javaFilesWithMoreThan76Lines"
