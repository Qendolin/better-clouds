## Use of telemetry data

As a developer of a mod designed to enhance gaming experiences, I want to explain why I collect anonymous telemetry data. 
By gathering this information, I aim to improve compatibility with different GPUs and GPU drivers. 
I understand the importance of privacy, and I want to assure you that I take it seriously. 
I only collect necessary data, and it is never sent regularly or used for any other purpose than enhancing the mod.

The anonymous telemetry data I collect provides valuable insights into the diverse hardware configurations and software environments that players use. 
This data helps me identify patterns and compatibility issues, allowing me to optimize the mod accordingly. 
Understanding which GPUs and drivers are commonly used enables me to prioritize testing and development efforts, ensuring the mod works seamlessly for a wide range of players.

I want to emphasize that your privacy is respected. I collect only the information that is essential for improving compatibility, 
and I make sure the telemetry data remains anonymous.

## When is telemetry data collected

The mod collects `SYSTEM_INFORMATION` telemetry during the initial launch of the game with the mod loaded. 
Please note that if you upgrade to a newer version of the mod, this information may be retransmitted.

Additionally, `SHADER_COMPILE_ERROR` telemetry is collected when a shader fails to compile. 
This allows me to identify and address any issues related to shader compilation promptly.

## What information is included in the telemetry dta

`META_INFORMATION` includes:
- `modVersion` The version of this mod
- `modSemVer` The version of this mod, but more detailed
- `mcVersion` The version of the game
- `mcSemVer` The version of the game, but more detailed

`SYSTEM_INFORMATION` telemetry includes:

- `META_INFORMATION`
- `os` The name of your Operating System
- `vendor` The name of your GPU Vendor
- `renderer` The name of your GPU
- `glVersion` The name of your OpenGL implementation
- `glVersionMajor` The major version of your OpenGL version
- `glVersionMinor` The minor version of your OpenGL version
- `glVersionCombined` The major and minor version of your OpenGL version
- `glVersionLwjgl` The OpenGL version reported by LWJGL
- `glslVersion` The GLSL version
- `extensions` A list of supported, selected, OpenGL extensions
- `functions` A list of supported, selected, OpenGL functions
- `cpuName` The name of your CPU
- `compatible` If your system is compatible with the mod

`SHADER_COMPILE_ERROR` telemetry includes

- `payload` The shader compiler log and the shader source code
- `SYSTEM_INFORMATION`

`UNHANDLED_EXCEPTION` telemetry includes

- `payload` The exception message and it's stack trace
- `SYSTEM_INFORMATION`


Additionally, by the nature of HTTP requests your IP address is transmitted to the collection server, 
but it is not stored.

## Where is the telemetry data stored

The data is securely stored on Google server located in the EU