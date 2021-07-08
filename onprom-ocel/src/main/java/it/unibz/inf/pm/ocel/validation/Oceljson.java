package it.unibz.inf.pm.ocel.validation;

public class Oceljson {
    public static boolean apply(String input_path, String validation_path, String parameters){
//        file_content = json.load(open(input_path, "rb"))
//        schema_content = json.load(open(validation_path, "rb"))
        try {
           // validate(instance=file_content, schema=schema_content);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try:
//        validate(instance=file_content, schema=schema_content)
//        return True
//        except jsonschema.
//        exceptions.ValidationError as err:
//        return False
        return true;
    }


}
