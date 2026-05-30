const express = require("express");
const cors = require("cors");
const { MongoClient, ObjectId } = require("mongodb");
require("dotenv").config();

const app = express();

app.use(cors());
app.use(express.json());

const PORT = process.env.PORT || 3000;
const MONGO_URI = process.env.MONGO_URI;
const DB_NAME = process.env.DB_NAME || "commov_db";

let db;

async function connectToMongo() {
  try {
    if (!MONGO_URI) {
      console.error("Erro: MONGO_URI não está definida no ficheiro .env");
      process.exit(1);
    }

    const client = new MongoClient(MONGO_URI);
    await client.connect();

    db = client.db(DB_NAME);

    console.log("Ligação ao MongoDB feita com sucesso.");
    console.log("Base de dados:", DB_NAME);
  } catch (error) {
    console.error("Erro ao ligar ao MongoDB:", error.message);
    process.exit(1);
  }
}

app.get("/", (req, res) => {
  res.json({
    message: "API COMMOV a funcionar",
    database: DB_NAME
  });
});

app.get("/health", async (req, res) => {
  try {
    await db.command({ ping: 1 });

    res.json({
      status: "OK",
      message: "Backend ligado ao MongoDB Atlas"
    });
  } catch (error) {
    res.status(500).json({
      status: "ERROR",
      message: "Erro na ligação ao MongoDB",
      error: error.message
    });
  }
});

app.get("/collections", async (req, res) => {
  try {
    const collections = await db.listCollections().toArray();
    res.json(collections.map(collection => collection.name));
  } catch (error) {
    res.status(500).json({
      message: "Erro ao listar coleções",
      error: error.message
    });
  }
});

app.get("/roles", async (req, res) => {
  try {
    const roles = await db.collection("roles").find().toArray();
    res.json(roles);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar roles",
      error: error.message
    });
  }
});

app.get("/users", async (req, res) => {
  try {
    const users = await db.collection("users").find().toArray();
    res.json(users);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar utilizadores",
      error: error.message
    });
  }
});

app.post("/users", async (req, res) => {
  try {
    const user = {
      firstName: req.body.firstName,
      lastName: req.body.lastName,
      email: req.body.email,
      passwordHash: req.body.passwordHash,
      status: req.body.status || "active",
      roleId: new ObjectId(req.body.roleId),
      createdAt: new Date(),
      updatedAt: new Date()
    };

    const result = await db.collection("users").insertOne(user);

    res.status(201).json({
      message: "Utilizador criado com sucesso",
      insertedId: result.insertedId
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao criar utilizador",
      error: error.message
    });
  }
});

app.get("/internship-offers", async (req, res) => {
  try {
    const offers = await db.collection("internshipOffers").find().toArray();
    res.json(offers);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar ofertas de estágio",
      error: error.message
    });
  }
});

app.get("/applications", async (req, res) => {
  try {
    const applications = await db.collection("applications").find().toArray();
    res.json(applications);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar candidaturas",
      error: error.message
    });
  }
});

connectToMongo().then(() => {
  app.listen(PORT, () => {
    console.log(`Servidor a correr em http://localhost:${PORT}`);
  });
});