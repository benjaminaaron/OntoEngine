import CreatableSelect from 'react-select/creatable';

export default function AutocompleteCreatable() {

    const options = [
        { value: 'chocolate', label: 'Chocolate' },
        { value: 'strawberry', label: 'Strawberry' },
        { value: 'vanilla', label: 'Vanilla' }
    ];

    const handleChange = e => {
        if (e.__isNew__) {
            console.log("new: ", e);
        } else {
            console.log("existing: ", e);
        }
    }

    return (
      <CreatableSelect
        onChange={handleChange}
        // onInputChange={handleInputChange}
        options={options}
      />
    );
}
