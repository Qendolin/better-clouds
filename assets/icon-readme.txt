/time set 23500
/tp @s ~ -64 ~ -90 -45

Run this command to resize & compress the icon (requires Node):
npx @squoosh/cli --resize '{"enabled":true,"width":256,"height":256,"method":"mitchell","fitMethod":"stretch","premultiply":true,"linearRGB":true}' --oxipng '{"level":3,"interlace":false}'