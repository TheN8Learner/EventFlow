const fs = require('fs');
const path = require('path');

const apiBaseUrl = process.env.API_BASE_URL;
const cloudinaryCloudName = process.env.CLOUDINARY_CLOUD_NAME;
const cloudinaryUploadPreset = process.env.CLOUDINARY_UPLOAD_PRESET;

if (!apiBaseUrl) {
  console.error('Missing API_BASE_URL. Example: API_BASE_URL=https://eventflow-api.onrender.com');
  process.exit(1);
}

if (!cloudinaryCloudName || !cloudinaryUploadPreset) {
  console.error('Missing CLOUDINARY_CLOUD_NAME or CLOUDINARY_UPLOAD_PRESET.');
  process.exit(1);
}

const environmentFile = `export const environment = {
  production: true,
  apiBaseUrl: '${apiBaseUrl.replace(/\/$/, '')}',
  apiUrl: '/api/v1',
  cloudinary: {
    cloudName: '${cloudinaryCloudName}',
    uploadPreset: '${cloudinaryUploadPreset}'
  }
};
`;

const target = path.join(__dirname, '..', 'src', 'environments', 'environment.prod.ts');
fs.writeFileSync(target, environmentFile);
console.log(`Wrote ${target}`);
