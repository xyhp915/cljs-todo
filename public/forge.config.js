const path = require('path')

module.exports = {
  packagerConfig: {
    icon: './assets/icon.icns'
  },

  makers: [
    {
      'name': '@electron-forge/maker-squirrel',
      'config': {
        'name': 'clj-todo-app'
      }
    },
    {
      name: '@electron-forge/maker-dmg',
      config: {
        background: './assets/bg.png',
        format: 'ULFO',
        icon: './assets/icon.icns',
        name: 'CLJS ToDo®'
      }
    },
    {
      name: '@electron-forge/maker-zip',
      platforms: ['darwin', 'linux']
    }
  ]
}
