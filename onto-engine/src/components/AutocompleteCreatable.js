import CreatableSelect from 'react-select/creatable';

export default function AutocompleteCreatable() {

    const options = [
        { value: 'chocolate', label: 'Chocolate' },
        { value: 'strawberry', label: 'Strawberry' },
        { value: 'vanilla', label: 'Vanilla' }
    ];

    const handleChange = e => {
        console.log("selected existing: ", e);
    };

    const handleCreation = e => {
        console.log("created ", e);
    };

    return (
      <CreatableSelect
        onChange={handleChange}
        onCreateOption={handleCreation}
        options={options}
      />
    );
}
