cd run
rem This line is just copied from the intellij idea console, but the agentlib arg is removed and -Dfabric.development=true added
"C:\Program Files\Eclipse Adoptium\jdk-17.0.1.12-hotspot\bin\java.exe" -Dfabric.development=true -Dfabric.dli.config=C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\launch.cfg -Dfabric.dli.env=client -Dfabric.dli.main=net.fabricmc.loader.impl.launch.knot.KnotClient -javaagent:c:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.fabricmc\sponge-mixin\0.12.4+mixin.0.8.5\e13b4069fe3555969811d4474f78576c707bbe1c\sponge-mixin-0.12.4+mixin.0.8.5.jar -javaagent:C:\Users\ICH\AppData\Local\JetBrains\IdeaIC2021.2\groovyHotSwap\gragent.jar -javaagent:C:\Users\ICH\AppData\Local\JetBrains\IdeaIC2021.2\captureAgent\debugger-agent.jar=file:/C:/Users/ICH/AppData/Local/Temp/capture6.props -Dfile.encoding=UTF-8 -classpath "C:\Users\ICH\Documents\_dev\cloud\better-clouds\build\production\better-clouds.main;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.fabricmc\tiny-mappings-parser\0.3.0+build.17\2f10540a290e382a7cd35c16ec3900046a4e252\tiny-mappings-parser-0.3.0+build.17.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.fabricmc\sponge-mixin\0.12.4+mixin.0.8.5\e13b4069fe3555969811d4474f78576c707bbe1c\sponge-mixin-0.12.4+mixin.0.8.5.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.fabricmc\tiny-remapper\0.8.2\2cc6565989469ec38893bfb7802b31c0d0d11ea0\tiny-remapper-0.8.2.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.fabricmc\access-widener\2.1.0\f62a27adbfd8ab4d4fa5681793039f2c0b177155\access-widener-2.1.0.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm\9.4\b4e0e2d2e023aa317b7cfcfc916377ea348e07d1\asm-9.4.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm-analysis\9.4\a5fec9dfc039448d4fd098fbaffcaf55373b223\asm-analysis-9.4.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm-commons\9.4\8fc2810ddbcbbec0a8bbccb3f8eda58321839912\asm-commons-9.4.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm-tree\9.4\a99175a17d7fdc18cbcbd0e8ea6a5d276844190a\asm-tree-9.4.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.ow2.asm\asm-util\9.4\ab1e0a84b72561dbaf1ee260321e72148ebf4b19\asm-util-9.4.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.github.oshi\oshi-core\6.2.2\54f5efc19bca95d709d9a37d19ffcbba3d21c1a6\oshi-core-6.2.2.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.google.code.gson\gson\2.10\dd9b193aef96e973d5a11ab13cd17430c2e4306b\gson-2.10.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.google.guava\failureaccess\1.0.1\1dcf1de382a0bf95a3d8b0849546c88bac1292c9\failureaccess-1.0.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.google.guava\guava\31.1-jre\60458f877d055d0c9114d9e1a2efb737b4bc282c\guava-31.1-jre.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.ibm.icu\icu4j\71.1\9e7d3304c23f9ba5cb71915f7cce23231a57a445\icu4j-71.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.mojang\authlib\3.18.38\16106b26bce62bda55bab42785b999e44d77ecb\authlib-3.18.38.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.mojang\blocklist\1.0.10\5c685c5ffa94c4cd39496c7184c1d122e515ecef\blocklist-1.0.10.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.mojang\brigadier\1.0.18\c1ef1234282716483c92183f49bef47b1a89bfa9\brigadier-1.0.18.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.mojang\datafixerupper\6.0.6\e38e20946530646e866db03b2b192883d0ea6e84\datafixerupper-6.0.6.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.mojang\logging\1.1.1\832b8e6674a9b325a5175a3a6267dfaf34c85139\logging-1.1.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.mojang\patchy\2.2.10\da05971b07cbb379d002cf7eaec6a2048211fefc\patchy-2.2.10.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\com.mojang\text2speech\1.16.7\ee4095669061d1fe4bce5fea23d69d1520bc2d58\text2speech-1.16.7.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\commons-codec\commons-codec\1.15\49d94806b6e3dc933dacbd8acb0fdbab8ebd1e5d\commons-codec-1.15.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\commons-io\commons-io\2.11.0\a2503f302b11ebde7ebc3df41daebe0e4eea3689\commons-io-2.11.0.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\commons-logging\commons-logging\1.2\4bfc12adfe4842bf07b657f0369c4cb522955686\commons-logging-1.2.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-buffer\4.1.82.Final\a544270cf1ae8b8077082f5036436a9a9971ea71\netty-buffer-4.1.82.Final.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-codec\4.1.82.Final\b77200379acb345a9ffdece1c605e591ac3e4e0a\netty-codec-4.1.82.Final.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-common\4.1.82.Final\22d148e85c3f5ebdacc0ce1f5aabb1d420f73f3\netty-common-4.1.82.Final.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-handler\4.1.82.Final\644041d1fa96a5d3130a29e8978630d716d76e38\netty-handler-4.1.82.Final.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-resolver\4.1.82.Final\38f665ae8dcd29032eea31245ba7806bed2e0fa8\netty-resolver-4.1.82.Final.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-transport-classes-epoll\4.1.82.Final\e7c7dd18deac93105797f30057c912651ea76521\netty-transport-classes-epoll-4.1.82.Final.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-transport-native-unix-common\4.1.82.Final\3e895b35ca1b8a0eca56cacff4c2dde5d2c6abce\netty-transport-native-unix-common-4.1.82.Final.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-transport\4.1.82.Final\e431a218d91acb6476ccad5f5aafde50aa3945ca\netty-transport-4.1.82.Final.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\it.unimi.dsi\fastutil\8.5.9\bb7ea75ecdb216654237830b3a96d87ad91f8cc5\fastutil-8.5.9.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.java.dev.jna\jna-platform\5.12.1\97406a297c852f4a41e688a176ec675f72e8329\jna-platform-5.12.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.java.dev.jna\jna\5.12.1\b1e93a735caea94f503e95e6fe79bf9cdc1e985d\jna-5.12.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.sf.jopt-simple\jopt-simple\5.0.4\4fdac2fbe92dfad86aa6e9301736f6b4342a3f5c\jopt-simple-5.0.4.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.apache.commons\commons-compress\1.21\4ec95b60d4e86b5c95a0e919cb172a0af98011ef\commons-compress-1.21.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.apache.commons\commons-lang3\3.12.0\c6842c86792ff03b9f1d1fe2aab8dc23aa6c6f0e\commons-lang3-3.12.0.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.apache.httpcomponents\httpclient\4.5.13\e5f6cae5ca7ecaac1ec2827a9e2d65ae2869cada\httpclient-4.5.13.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.apache.httpcomponents\httpcore\4.4.15\7f2e0c573eaa7a74bac2e89b359e1f73d92a0a1d\httpcore-4.4.15.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.apache.logging.log4j\log4j-api\2.19.0\ea1b37f38c327596b216542bc636cfdc0b8036fa\log4j-api-2.19.0.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.apache.logging.log4j\log4j-core\2.19.0\3b6eeb4de4c49c0fe38a4ee27188ff5fee44d0bb\log4j-core-2.19.0.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.apache.logging.log4j\log4j-slf4j2-impl\2.19.0\5c04bfdd63ce9dceb2e284b81e96b6a70010ee10\log4j-slf4j2-impl-2.19.0.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.joml\joml\1.10.5\22566d58af70ad3d72308bab63b8339906deb649\joml-1.10.5.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-glfw\3.3.1\cbac1b8d30cb4795149c1ef540f912671a8616d0\lwjgl-glfw-3.3.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-glfw\3.3.1\ed892f945cf7e79c8756796f32d00fa4ceaf573b\lwjgl-glfw-3.3.1-natives-windows.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-glfw\3.3.1\beda65ee503443e60aa196d58ed31f8d001dc22a\lwjgl-glfw-3.3.1-natives-windows-arm64.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-glfw\3.3.1\b997e3391d6ce8f05487e7335d95c606043884a1\lwjgl-glfw-3.3.1-natives-windows-x86.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-jemalloc\3.3.1\a817bcf213db49f710603677457567c37d53e103\lwjgl-jemalloc-3.3.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-jemalloc\3.3.1\948a89b76a16aa324b046ae9308891216ffce5f9\lwjgl-jemalloc-3.3.1-natives-windows.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-jemalloc\3.3.1\cae85c4edb219c88b6a0c26a87955ad98dc9519d\lwjgl-jemalloc-3.3.1-natives-windows-arm64.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-jemalloc\3.3.1\fb476c8ec110e1c137ad3ce8a7f7bfe6b11c6324\lwjgl-jemalloc-3.3.1-natives-windows-x86.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-openal\3.3.1\2623a6b8ae1dfcd880738656a9f0243d2e6840bd\lwjgl-openal-3.3.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-openal\3.3.1\30a474d0e57193d7bc128849a3ab66bc9316fdb1\lwjgl-openal-3.3.1-natives-windows.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-openal\3.3.1\40d65f1a7368a2aa47336f9cb69f5a190cf9975a\lwjgl-openal-3.3.1-natives-windows-arm64.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-openal\3.3.1\888349f7b1be6fbae58bf8edfb9ef12def04c4e3\lwjgl-openal-3.3.1-natives-windows-x86.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-opengl\3.3.1\831a5533a21a5f4f81bbc51bb13e9899319b5411\lwjgl-opengl-3.3.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-opengl\3.3.1\c1807e9bd571402787d7e37e3029776ae2513bb8\lwjgl-opengl-3.3.1-natives-windows.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-opengl\3.3.1\527d78f1e9056aff3ed02ce93019c73c5e8f1721\lwjgl-opengl-3.3.1-natives-windows-arm64.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-opengl\3.3.1\deef3eb9b178ff2ff3ce893cc72ae741c3a17974\lwjgl-opengl-3.3.1-natives-windows-x86.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-stb\3.3.1\b119297cf8ed01f247abe8685857f8e7fcf5980f\lwjgl-stb-3.3.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-stb\3.3.1\86315914ac119efdb02dc9e8e978ade84f1702af\lwjgl-stb-3.3.1-natives-windows.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-stb\3.3.1\fde63cdd2605c00636721a6c8b961e41d1f6b247\lwjgl-stb-3.3.1-natives-windows-arm64.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-stb\3.3.1\a8d41f419eecb430b7c91ea2ce2c5c451cae2091\lwjgl-stb-3.3.1-natives-windows-x86.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-tinyfd\3.3.1\ff1914111ef2e3e0110ef2dabc8d8cdaad82347\lwjgl-tinyfd-3.3.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-tinyfd\3.3.1\a5d830475ec0958d9fdba1559efa99aef211e6ff\lwjgl-tinyfd-3.3.1-natives-windows.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-tinyfd\3.3.1\83a5e780df610829ff3a737822b4f931cffecd91\lwjgl-tinyfd-3.3.1-natives-windows-arm64.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl-tinyfd\3.3.1\842eedd876fae354abc308c98a263f6bbc9e8a4d\lwjgl-tinyfd-3.3.1-natives-windows-x86.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl\3.3.1\ae58664f88e18a9bb2c77b063833ca7aaec484cb\lwjgl-3.3.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl\3.3.1\36c37f16ab611b3aa11f3bcf80b1d509b4ce6b\lwjgl-3.3.1-natives-windows.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl\3.3.1\f46cadcf95675908fd3a550d63d9d709cb68998\lwjgl-3.3.1-natives-windows-arm64.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.lwjgl\lwjgl\3.3.1\3b14f4beae9dd39791ec9e12190a9380cd8a3ce6\lwjgl-3.3.1-natives-windows-x86.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.slf4j\slf4j-api\2.0.1\f48d81adce2abf5ad3cfe463df517952749e03bc\slf4j-api-2.0.1.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\1.19.4\net.fabricmc.yarn.1_19_4.1.19.4+build.2-v2\minecraft-project-@-merged-named.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-transport-native-epoll\4.1.82.Final\476409d6255001ca53a55f65b01c13822f8dc93a\netty-transport-native-epoll-4.1.82.Final-linux-aarch_64.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\io.netty\netty-transport-native-epoll\4.1.82.Final\c7350a71920f3ae9142945e25fed4846cce53374\netty-transport-native-epoll-4.1.82.Final-linux-x86_64.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\dev\isxander\yacl\yet-another-config-lib-fabric\2.5.1+1.19.4\yet-another-config-lib-fabric-2.5.1+1.19.4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\com\terraformersmc\modmenu\6.2.2\modmenu-6.2.2.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-loader\0.14.19\fabric-loader-0.14.19.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-api\0.81.1+1.19.4\fabric-api-0.81.1+1.19.4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-transfer-api-v1\3.2.0+80d07a0af4\fabric-transfer-api-v1-3.2.0+80d07a0af4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-api-lookup-api-v1\1.6.27+504944c8f4\fabric-api-lookup-api-v1-1.6.27+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-blockrenderlayer-v1\1.1.36+c2e6f674f4\fabric-blockrenderlayer-v1-1.1.36+c2e6f674f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-client-tags-api-v1\1.0.17+504944c8f4\fabric-client-tags-api-v1-1.0.17+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-command-api-v2\2.2.8+504944c8f4\fabric-command-api-v2-2.2.8+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-content-registries-v0\3.5.11+ae0966baf4\fabric-content-registries-v0-3.5.11+ae0966baf4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-convention-tags-v1\1.5.1+fe8721bef4\fabric-convention-tags-v1-1.5.1+fe8721bef4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-data-generation-api-v1\11.4.2+504944c8f4\fabric-data-generation-api-v1-11.4.2+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-dimensions-v1\2.1.47+7f87f8faf4\fabric-dimensions-v1-2.1.47+7f87f8faf4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-entity-events-v1\1.5.15+504944c8f4\fabric-entity-events-v1-1.5.15+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-events-interaction-v0\0.5.1+76ba65ebf4\fabric-events-interaction-v0-0.5.1+76ba65ebf4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-gametest-api-v1\1.2.6+ae0966baf4\fabric-gametest-api-v1-1.2.6+ae0966baf4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-item-api-v1\2.1.19+504944c8f4\fabric-item-api-v1-2.1.19+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-item-group-api-v1\3.0.7+043f9acff4\fabric-item-group-api-v1-3.0.7+043f9acff4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-mining-level-api-v1\2.1.41+49abcf7ef4\fabric-mining-level-api-v1-2.1.41+49abcf7ef4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-lifecycle-events-v1\2.2.17+1e9487d2f4\fabric-lifecycle-events-v1-2.2.17+1e9487d2f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-loot-api-v2\1.1.29+75e98211f4\fabric-loot-api-v2-1.1.29+75e98211f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-message-api-v1\5.1.3+504944c8f4\fabric-message-api-v1-5.1.3+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-models-v0\0.3.32+504944c8f4\fabric-models-v0-0.3.32+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-recipe-api-v1\1.0.10+a1ccd7bff4\fabric-recipe-api-v1-1.0.10+a1ccd7bff4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-registry-sync-v0\2.2.2+504944c8f4\fabric-registry-sync-v0-2.2.2+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-screen-handler-api-v1\1.3.22+504944c8f4\fabric-screen-handler-api-v1-1.3.22+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-networking-api-v1\1.3.3+504944c8f4\fabric-networking-api-v1-1.3.3+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-object-builder-api-v1\7.0.5+504944c8f4\fabric-object-builder-api-v1-7.0.5+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-particles-v1\1.0.25+f1e4495bf4\fabric-particles-v1-1.0.25+f1e4495bf4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-renderer-indigo\1.2.0+ebc93ff3f4\fabric-renderer-indigo-1.2.0+ebc93ff3f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-renderer-api-v1\2.2.7+ebc93ff3f4\fabric-renderer-api-v1-2.2.7+ebc93ff3f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-rendering-data-attachment-v1\0.3.30+afca2f3ef4\fabric-rendering-data-attachment-v1-0.3.30+afca2f3ef4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-rendering-fluids-v1\3.0.23+504944c8f4\fabric-rendering-fluids-v1-3.0.23+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-rendering-v1\2.1.3+504944c8f4\fabric-rendering-v1-2.1.3+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-screen-api-v1\1.0.47+3bd4ab0ff4\fabric-screen-api-v1-1.0.47+3bd4ab0ff4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-api-base\0.4.26+1e9487d2f4\fabric-api-base-0.4.26+1e9487d2f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-biome-api-v1\13.0.8+348a9c64f4\fabric-biome-api-v1-13.0.8+348a9c64f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-block-api-v1\1.0.7+e022e5d1f4\fabric-block-api-v1-1.0.7+e022e5d1f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-crash-report-info-v1\0.2.16+aeb40ebef4\fabric-crash-report-info-v1-0.2.16+aeb40ebef4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-game-rule-api-v1\1.0.34+a1ccd7bff4\fabric-game-rule-api-v1-1.0.34+a1ccd7bff4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-key-binding-api-v1\1.0.34+504944c8f4\fabric-key-binding-api-v1-1.0.34+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-resource-conditions-api-v1\2.3.2+e6c7d4eef4\fabric-resource-conditions-api-v1-2.3.2+e6c7d4eef4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-resource-loader-v0\0.11.4+5ade3c38f4\fabric-resource-loader-v0-0.11.4+5ade3c38f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-sound-api-v1\1.0.10+504944c8f4\fabric-sound-api-v1-1.0.10+504944c8f4.jar;C:\Users\ICH\Documents\_dev\cloud\better-clouds\.gradle\loom-cache\remapped_mods\net_fabricmc_yarn_1_19_4_1_19_4_build_2_v2\net\fabricmc\fabric-api\fabric-transitive-access-wideners-v1\3.1.1+b4a333d6f4\fabric-transitive-access-wideners-v1-3.1.1+b4a333d6f4.jar;C:\Users\ICH\.gradle\caches\fabric-loom\1.19.4\net.fabricmc.yarn.1_19_4.1.19.4+build.2-v2\mappings.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.fabricmc\dev-launch-injector\0.2.1+build.8\da8bef7e6e2f952da707f282bdb46882a0fce5e3\dev-launch-injector-0.2.1+build.8.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\net.minecrell\terminalconsoleappender\1.2.0\96d02cd3b384ff015a8fef4223bcb4ccf1717c95\terminalconsoleappender-1.2.0.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.jline\jline-reader\3.12.1\4382ab1382c7b6f379377ed5f665dc2f6e1218bc\jline-reader-3.12.1.jar;C:\Users\ICH\.gradle\caches\modules-2\files-2.1\org.jline\jline-terminal\3.12.1\c777448314e050d980a6b697c140f3bfe9eb7416\jline-terminal-3.12.1.jar;C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2021.2.2\lib\idea_rt.jar" net.fabricmc.devlaunchinjector.Main

pause