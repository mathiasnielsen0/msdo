package example;

import static spark.Spark.*;

public class HelloSpark {
	public static void main(String[] args) {
		get("/hello/:name",
			(req, res) -> "<H2>Hello to you "+req.params(":name")+"</H2>");
	}
}