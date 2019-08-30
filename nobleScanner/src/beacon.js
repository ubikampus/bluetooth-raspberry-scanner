class Beacon {
    constructor(id) {
        this.id = id;
        this.observations = [];
    }

    addObservation(observation) {
        this.observations.push(observation);

        if (this.observations.length > 10) {
            this.observations.shift();
        } 
    }

    average() {
        const sum = this.observations.reduce((a, c) => a+c);
        return sum / this.observations.length;
    }
}

module.exports = Beacon;
