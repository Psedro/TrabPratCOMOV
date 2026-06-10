const bcrypt = require("bcryptjs");
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

function separarNomeCompleto(nome) {
  const partes = (nome || "").trim().split(/\s+/);
  const firstName = partes.shift() || "";
  const lastName = partes.join(" ");
  return { firstName, lastName };
}

function obterLabelRole(tipo) {
  switch (tipo) {
    case "student":
      return "Estudante";
    case "teacher":
      return "Professor";
    case "company":
      return "Empresa";
    default:
      return "Utilizador";
  }
}

async function obterEnderecoPadrao() {
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

  return address;
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
    const { internshipOfferId, userId, coverLetter, availableFrom } = req.body;

    if (!internshipOfferId || !userId) {
      return res.status(400).json({
        message: "internshipOfferId e userId são obrigatórios"
      });
    }

    const user = await db.collection("users").findOne({
      _id: new ObjectId(userId)
    });

    if (!user) {
      return res.status(404).json({
        message: "Utilizador não encontrado"
      });
    }

    const student = await db.collection("students").findOne({
      userId: user._id
    });

    if (!student) {
      return res.status(404).json({
        message: "Aluno não encontrado"
      });
    }

    const offer = await db.collection("internshipOffers").findOne({
      _id: new ObjectId(internshipOfferId)
    });

    if (!offer) {
      return res.status(404).json({
        message: "Oferta não encontrada"
      });
    }

    const existingApplication = await db.collection("applications").findOne({
      studentId: student._id,
      internshipOfferId: offer._id
    });

    if (existingApplication) {
      return res.status(409).json({
        message: "Já existe uma candidatura para esta oferta"
      });
    }

    const application = {
      appliedDate: new Date(),
      status: "pending",
      coverLetter: coverLetter || "Candidatura submetida através da aplicação móvel.",
      availableFrom: availableFrom ? new Date(availableFrom) : new Date(),
      portfolioUrl: req.body.portfolioUrl || "",
      cvDocumentId: null,
      studentId: student._id,
      internshipOfferId: offer._id,
      createdAt: new Date(),
      updatedAt: new Date()
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

app.get("/student-dashboard", async (req, res) => {
  try {
    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({
        message: "userId é obrigatório"
      });
    }

    const user = await db.collection("users").findOne({
      _id: new ObjectId(userId)
    });

    if (!user) {
      return res.status(404).json({
        message: "Utilizador não encontrado"
      });
    }

    const student = await db.collection("students").findOne({
      userId: user._id
    });

    if (!student) {
      return res.json({
        nomeUtilizador: `${user.firstName || ""} ${user.lastName || ""}`.trim().toUpperCase(),
        candidaturasAtivas: 0,
        candidaturasAceites: 0,
        mensagensNovas: 0
      });
    }

    const candidaturas = await db.collection("applications").find({
      studentId: student._id
    }).toArray();

    const candidaturasAtivas = candidaturas.filter(app =>
      ["pending", "accepted", "ongoing", "in_progress"].includes(app.status)
    ).length;

    const candidaturasAceites = candidaturas.filter(app =>
      app.status === "accepted"
    ).length;

    const mensagensNovas = await db.collection("messages").countDocuments({
      receiverId: user._id,
      isRead: false
    });

    res.json({
      nomeUtilizador: `${user.firstName || ""} ${user.lastName || ""}`.trim().toUpperCase(),
      candidaturasAtivas,
      candidaturasAceites,
      mensagensNovas
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar dados do dashboard",
      error: error.message
    });
  }
});

app.get("/student-applications", async (req, res) => {
  try {
    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({
        message: "userId é obrigatório"
      });
    }

    const user = await db.collection("users").findOne({
      _id: new ObjectId(userId)
    });

    if (!user) {
      return res.status(404).json({
        message: "Utilizador não encontrado"
      });
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
          cvName: "CV_Aluno.pdf",
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

app.post("/api/auth/register", async (req, res) => {
  try {
    const { nome, email, username, password, tipo, estudante, professor, empresa } = req.body;

    if (!nome || !email || !username || !password || !tipo) {
      return res.status(400).json({
        message: "Nome, email, username, password e tipo são obrigatórios"
      });
    }

    const tiposValidos = ["student", "teacher", "company"];

    if (!tiposValidos.includes(tipo)) {
      return res.status(400).json({
        message: "Tipo de utilizador inválido"
      });
    }

    const emailNormalizado = email.trim().toLowerCase();
    const usernameNormalizado = username.trim();

    const existingUser = await db.collection("users").findOne({
      $or: [
        { email: emailNormalizado },
        { username: usernameNormalizado }
      ]
    });

    if (existingUser) {
      return res.status(409).json({
        message: "Email ou username já existe"
      });
    }

    let role = await db.collection("roles").findOne({
      name: tipo
    });

    if (!role) {
      const roleResult = await db.collection("roles").insertOne({
        name: tipo,
        description: obterLabelRole(tipo),
        createdAt: new Date(),
        updatedAt: new Date()
      });

      role = {
        _id: roleResult.insertedId,
        name: tipo,
        description: obterLabelRole(tipo)
      };
    }

    let firstName;
    let lastName;

    if (tipo === "company") {
      firstName = empresa.nomeEmpresa.trim();
      lastName = "";
    } else {
      const nomeSeparado = separarNomeCompleto(nome);
      firstName = nomeSeparado.firstName;
      lastName = nomeSeparado.lastName;
    }

    const user = {
      firstName,
      lastName,
      username: usernameNormalizado,
      email: emailNormalizado,
      passwordHash: await bcrypt.hash(password, 10),
      status: "active",
      roleId: role._id,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    const userResult = await db.collection("users").insertOne(user);
    const userId = userResult.insertedId;

    if (tipo === "student") {
      const address = await obterEnderecoPadrao();

      await db.collection("students").insertOne({
        userId: userId,
        indexNumber: Number(estudante.numeroAluno),
        studyYear: Number(estudante.ano),
        degreeLevel: estudante.curso || "Licenciatura",
        addressId: address._id,
        mainCvId: null
      });
    }

    if (tipo === "teacher") {
      await db.collection("teachers").insertOne({
        userId: userId,
        teacherNumber: professor.numeroProfessor,
        department: professor.departamento,
        createdAt: new Date(),
        updatedAt: new Date()
      });
    }

    if (tipo === "company") {
      await db.collection("companies").insertOne({
        ownerUserId: userId,
        name: empresa.nomeEmpresa,
        website: empresa.website || "",
        description: empresa.descricao || "",
        industryIds: [],
        createdAt: new Date(),
        updatedAt: new Date()
      });
    }

    res.status(201).json({
      message: "Utilizador registado com sucesso",
      user: {
        id: userId.toString(),
        nome: tipo === "company" ? empresa.nomeEmpresa : nome,
        email: emailNormalizado,
        username: usernameNormalizado,
        tipo: tipo,
        roleId: role._id.toString()
      }
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao registar utilizador",
      error: error.message
    });
  }
});

app.post("/api/auth/login", async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({
        message: "Email e password são obrigatórios"
      });
    }

    const emailNormalizado = email.trim().toLowerCase();

    const user = await db.collection("users").findOne({
      email: emailNormalizado
    });

    if (!user) {
      return res.status(401).json({
        message: "Email ou password inválidos"
      });
    }

    const passwordValida = await bcrypt.compare(password, user.passwordHash);

    if (!passwordValida) {
      return res.status(401).json({
        message: "Email ou password inválidos"
      });
    }

    const role = await db.collection("roles").findOne({
      _id: user.roleId
    });

    res.json({
      message: "Login efetuado com sucesso",
      user: {
        id: user._id.toString(),
        nome: `${user.firstName || ""} ${user.lastName || ""}`.trim(),
        email: user.email,
        username: user.username,
        roleId: user.roleId?.toString(),
        tipo: role?.name || null
      }
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao fazer login",
      error: error.message
    });
  }
});

connectToMongo().then(() => {
  app.listen(PORT, () => {
    console.log(`Servidor a correr em http://localhost:${PORT}`);
  });
});
