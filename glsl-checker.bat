@echo off
setlocal enabledelayedexpansion

REM Usage: glsl-checker.bat *
REM Checks the minimum version for all files in src\main\resources\assets\betterclouds\shaders\core
REM Requires glslangValidator.exe under ./local/
REM you can get it at https://github.com/KhronosGroup/glslang/releases/tag/main-tot

REM define any missing shader constants
set "defines=-D_SIZE_XZ_=1 -D_SIZE_Y_=1 -D_VISIBILITY_EDGE_=1"
set "glslangValidator=local\glslangValidator.exe"
set "folder=src\main\resources\assets\betterclouds\shaders\core"
set "versions=460, 450, 440, 430, 420, 410, 400, 330, 150, 140, 130, 120, 110"
set "stage="

REM Step 1: Create temporary folder ".validator"
rd /s /q ".validator"
mkdir ".validator"

REM Step 2: Copy files to temporary folder
xcopy /i /y /q %folder%\%1 ".validator"

REM Step 3: Process files in the temporary folder
for /r ".validator" %%F in (*.vsh, *.fsh) do (
    set "stage="
    set "filename=%%~nxF"

    REM Step 4: Determine the stage based on file extension
    if "%%~xF"==".vsh" (
        set "stage=vert"
    ) else if "%%~xF"==".fsh" (
        set "stage=frag"
    )

    if defined stage (
        REM Step 3.1: Iterate through the versions
		set skip=
        for %%V in (%versions%) do if not defined skip (
            set "version=%%V"

            REM Step 3.1.1: Replace the version line
            powershell -Command "(Get-Content '%%~fF') -replace '^#version .*$', '#version %%V' | Set-Content '%%~fF'"

            REM Step 3.1.2: Call glslangValidator
            %glslangValidator% -l %defines% -S !stage! "%%~fF" > .validator\latest.log

            if errorlevel 1 (
                REM Step 3.1.4: Command returned an error
                echo "!filename!" error for version !version!
				type .validator\latest.log
				set skip=yes
            ) else (
                REM Step 3.1.3: Command was successful
                echo "!filename!" can compile to !version!
            )
        )
    )
)

REM Step 5: Cleanup temporary folder
rd /s /q ".validator"
