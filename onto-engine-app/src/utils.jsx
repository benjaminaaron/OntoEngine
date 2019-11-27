
const getRandomArraySubset = (array, min, max) => {
    let shuffled = array.sort(() => 0.5 - Math.random()); // via stackoverflow.com/a/38571132/2474159
    return shuffled.slice(0, getRandomNumber(min, max));
}

const getRandomNumber = (min, max) => {
    return Math.floor(Math.random() * (max - min + 1)) + min; // via stackoverflow.com/a/1527834/2474159
};

// for boolean random: Math.random() >= 0.5 // via stackoverflow.com/a/36756480/2474159

export { getRandomArraySubset, getRandomNumber }
