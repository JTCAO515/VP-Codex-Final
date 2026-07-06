/** @type {import('next').NextConfig} */
const nextConfig = {
  allowedDevOrigins: [
    'localhost',
    '127.0.0.1',
    '10.2.94.54',
    ...(process.env.ALLOWED_DEV_ORIGINS?.split(',').map((s) => s.trim()).filter(Boolean) ?? []),
  ],
};

module.exports = nextConfig;
