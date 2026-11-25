'use strict';

const Urna = (() => {
  const offices = [
    {
      id: 'deputado', title: 'Deputado federal', digits: 4,
      candidates: [
        { number: '9101', name: 'Ana do Ipê', party: 'Partido do Ipê', color: 'moon' },
        { number: '9202', name: 'Lucas do Sol', party: 'Partido do Sol', color: 'sun' },
      ],
    },
    {
      id: 'presidente', title: 'Presidente', digits: 2,
      candidates: [
        { number: '91', name: 'Marina do Ipê', party: 'Partido do Ipê', color: 'moon' },
        { number: '92', name: 'Rafael do Sol', party: 'Partido do Sol', color: 'sun' },
      ],
    },
  ];

  class SessaoVotacao {
    #index = 0;
    #digits = '';
    #blank = false;
    #votes = {};

    get office() { return offices[this.#index]; }
    get digits() { return this.#digits; }
    get blank() { return this.#blank; }
    get ended() { return this.#index === offices.length; }
    get votes() { return { ...this.#votes }; }

    get canConfirm() {
      return !this.ended && (this.#blank || this.#digits.length === this.office.digits);
    }

    get candidate() {
      return this.office?.candidates.find(({ number }) => number === this.#digits);
    }

    enterDigit(digit) {
      if (this.ended || this.#blank || this.#digits.length === this.office.digits
          || !/^[0-9]$/.test(digit)) {
        return false;
      }
      this.#digits += digit;
      return true;
    }

    selectBlank() {
      if (this.ended || this.#blank) return false;
      this.#digits = '';
      this.#blank = true;
      return true;
    }

    correct() {
      if (this.ended || (!this.#digits && !this.#blank)) return false;
      this.#digits = '';
      this.#blank = false;
      return true;
    }

    confirm() {
      if (!this.canConfirm) return null;
      const type = this.#blank ? 'BRANCO' : this.candidate ? 'VÁLIDO' : 'NULO';
      const value = type === 'VÁLIDO' ? this.#digits : type;
      this.#votes[this.office.id] = value;
      this.#index += 1;
      this.#digits = '';
      this.#blank = false;
      return type;
    }
  }

  return { SessaoVotacao };
})();

if (typeof module !== 'undefined') module.exports = Urna;
