module.exports = {
  mode: 'jit',
  purge: ['./src/**/*.{js,jsx,ts,tsx}', './public/index.html'],
  darkMode: false, // or 'media' or 'class'
  theme: {
    extend: {
      colors: {
        classColor: {
          DEFAULT: '#cfa500',
        },
        objectPropertyColor: {
          DEFAULT: '#0079ba',
        },
        dataPropertyColor: {
          DEFAULT: '#38a14a',
        },
        individualsColor: {
          DEFAULT: '#874b82',
        },
        annotationPropertyColor: {
          DEFAULT: '#d17a00',
        },
        datatypesColor: {
          DEFAULT: '#ad3b45',
        },
        ontologyColor: {
          DEFAULT: '#6b47a2',
        },
      },
    },
  },
  variants: {
    extend: {},
  },
  plugins: [],
}
