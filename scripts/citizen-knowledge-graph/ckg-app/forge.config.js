module.exports = {
  packagerConfig: {
    "protocols": [
      {
        "name": "CKG App",
        "schemes": ["ckg-app"]
      }
    ]
  },
  makers: [
    {
      name: '@electron-forge/maker-zip',
      platforms: ['darwin'],
    }
  ],
};
