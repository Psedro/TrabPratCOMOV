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

app.post("/applications", async (req, res) => {
  try {
    const internshipOfferId = req.body.internshipOfferId;

    if (!internshipOfferId) {
      return res.status(400).json({
        message: "internshipOfferId é obrigatório"
      });
    }

    const role = await db.collection("roles").findOne({ name: "student" });

    let address = await db.collection("addresses").findOne({
      street: "Rua do Estudante Teste"
    });

    if (!address) {
      const addressResult = await db.collection("addresses").insertOne({
        street: "Rua do Estudante Teste",
        buildingNumber: "1",
        city: "Viana do Castelo",
        postalCode: "4900-000"
      });

      address = { _id: addressResult.insertedId };
    }

    let user = await db.collection("users").findOne({
      email: "pedro.sousa@alunos.ipvc.pt"
    });

    if (!user) {
      const userResult = await db.collection("users").insertOne({
        firstName: "Pedro",
        lastName: "Sousa",
        email: "pedro.sousa@alunos.ipvc.pt",
        passwordHash: "123456",
        status: "active",
        roleId: role._id,
        createdAt: new Date(),
        updatedAt: new Date()
      });

      user = { _id: userResult.insertedId };
    }

    let student = await db.collection("students").findOne({
      userId: user._id
    });

    if (!student) {
      const studentResult = await db.collection("students").insertOne({
        userId: user._id,
        indexNumber: 12345,
        studyYear: 3,
        degreeLevel: "Licenciatura",
        addressId: address._id,
        mainCvId: null
      });

      student = { _id: studentResult.insertedId };
    }

    const existingApplication = await db.collection("applications").findOne({
      studentId: student._id,
      internshipOfferId: new ObjectId(internshipOfferId)
    });

    if (existingApplication) {
      return res.status(409).json({
        message: "Já existe uma candidatura para esta oferta"
      });
    }

    const application = {
      appliedDate: new Date(),
      status: "pending",
      coverLetter: req.body.coverLetter || "Candidatura submetida através da aplicação móvel.",
      availableFrom: req.body.availableFrom ? new Date(req.body.availableFrom) : new Date(),
      portfolioUrl: req.body.portfolioUrl || "",
      cvDocumentId: null,
      studentId: student._id,
      internshipOfferId: new ObjectId(internshipOfferId)
    };

    const result = await db.collection("applications").insertOne(application);

    res.status(201).json({
      message: "Candidatura criada com sucesso",
      insertedId: result.insertedId
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao criar candidatura",
      error: error.message
    });
  }
});

app.get("/student-applications", async (req, res) => {
  try {
    const user = await db.collection("users").findOne({
      email: "pedro.sousa@alunos.ipvc.pt"
    });

    if (!user) {
      return res.json([]);
    }

    const student = await db.collection("students").findOne({
      userId: user._id
    });

    if (!student) {
      return res.json([]);
    }

    const applications = await db.collection("applications").aggregate([
      {
        $match: {
          studentId: student._id
        }
      },
      {
        $lookup: {
          from: "internshipOffers",
          localField: "internshipOfferId",
          foreignField: "_id",
          as: "offer"
        }
      },
      {
        $unwind: "$offer"
      },
      {
        $project: {
          _id: { $toString: "$_id" },
          status: 1,
          appliedDate: 1,
          cvName: "CV_Pedro_Sousa.pdf",
          offerTitle: "$offer.name",
          companyName: "$offer.companyName",
          offerDescription: "$offer.description",
          location: "$offer.location"
        }
      },
      {
        $sort: {
          appliedDate: -1
        }
      }
    ]).toArray();

    res.json(applications);
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar candidaturas do aluno",
      error: error.message
    });
  }
});

connectToMongo().then(() => {
  app.listen(PORT, () => {
    console.log(`Servidor a correr em http://localhost:${PORT}`);
  });
});