import sharp from 'sharp';
import { glob } from 'glob';

if (process.argv.length < 3) {
	console.log('usage: node index.js <in_glob>');
	process.exit();
}

const inArg = process.argv[2];

const filePaths = await glob(inArg);

if (filePaths.length == 0) {
	console.log("'%s' did not match any files", inArg);
	process.exit();
}

for (let i = 0; i < filePaths.length; i++) {
	const filePath = filePaths[i];
	console.log('Processing [%d/%d]: %s', i + 1, filePaths.length, filePath);
	sharp(filePath)
		.resize(1920, 1080)
		.webp({ quality: 85 })
		.toFile(filePath.replace(/\.[^.]+?$/g, '-lg.webp'));
	sharp(filePath)
		.resize(854, 480)
		.webp({ quality: 80 })
		.toFile(filePath.replace(/\.[^.]+?$/g, '-sm.webp'));
}
