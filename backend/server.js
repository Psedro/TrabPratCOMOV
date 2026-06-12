const bcrypt = require("bcryptjs");
const express = require("express");
const cors = require("cors");
const { MongoClient, ObjectId } = require("mongodb");
const multer = require("multer");
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");
require("dotenv").config();

const app = express();

app.use(cors());
app.use(express.json());

const uploadDir = path.join(__dirname, "uploads", "cvs");

fs.mkdirSync(uploadDir, { recursive: true });

app.use("/uploads", express.static(path.join(__dirname, "uploads")));

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    cb(null, `${crypto.randomUUID()}${ext}`);
  }
});

const upload = multer({
  storage,
  limits: {
    fileSize: 5 * 1024 * 1024
  },
  fileFilter: (req, file, cb) => {
    const ext = path.extname(file.originalname).toLowerCase();
    const allowedExtensions = [".pdf", ".doc", ".docx"];

    if (!allowedExtensions.includes(ext)) {
      return cb(new Error("O currículo deve ser PDF, DOC ou DOCX."));
    }

    cb(null, true);
  }
});

function apagarFicheiro(caminho) {
  if (!caminho) return;

  fs.unlink(caminho, (error) => {
    if (error) {
      console.error("Erro ao apagar ficheiro:", error.message);
    }
  });
}
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

app.post("/internship-offers", async (req, res) => {
  try {
    console.log("====== CRIAR OFERTA ======");
    console.log("BODY OFERTA:", req.body);

    const {
      userId,
      name,
      description,
      requirements,
      duration_in_months,
      total_spots,
      application_deadline,
      companyName,
      location
    } = req.body;

    const camposObrigatorios = {
      userId,
      name,
      description,
      requirements,
      duration_in_months,
      total_spots,
      application_deadline,
      location
    };

    console.log("CAMPOS RECEBIDOS OFERTA:", camposObrigatorios);

    const camposEmFalta = Object.entries(camposObrigatorios)
      .filter(([_, valor]) => valor === undefined || valor === null || valor === "")
      .map(([campo]) => campo);

    console.log("CAMPOS EM FALTA:", camposEmFalta);

    if (camposEmFalta.length > 0) {
      return res.status(400).json({
        message: "Campos obrigatórios em falta",
        camposEmFalta
      });
    }

    if (!ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const ownerUserId = new ObjectId(userId);

    const user = await db.collection("users").findOne({
      _id: ownerUserId
    });

    if (!user) {
      return res.status(404).json({
        message: "Utilizador não encontrado"
      });
    }

    const role = await db.collection("roles").findOne({
      _id: user.roleId
    });

    if (role?.name !== "company") {
      return res.status(403).json({
        message: "Apenas empresas podem criar ofertas"
      });
    }

    const company = await db.collection("companies").findOne({
      ownerUserId: ownerUserId
    });

    console.log("EMPRESA ENCONTRADA:", company);

    if (!company) {
      return res.status(404).json({
        message: "Empresa não encontrada para este utilizador"
      });
    }

    let industry = await db.collection("industries").findOne({
      name: "Tecnologia"
    });

    if (!industry) {
      const industryResult = await db.collection("industries").insertOne({
        name: "Tecnologia",
        description: "Área tecnológica",
        createdAt: new Date(),
        updatedAt: new Date()
      });

      industry = {
        _id: industryResult.insertedId,
        name: "Tecnologia"
      };
    }

    let companyLocation = await db.collection("companyLocations").findOne({
      $or: [
        {
          companyId: company._id,
          isHeadquarters: true
        },
        {
          companyId: company._id.toString(),
          isHeadquarters: true
        }
      ]
    });

    if (!companyLocation) {
      companyLocation = await db.collection("companyLocations").findOne({
        $or: [
          { companyId: company._id },
          { companyId: company._id.toString() },
          { company_id: company._id },
          { company_id: company._id.toString() }
        ]
      });
    }

    console.log("LOCALIZAÇÃO DA EMPRESA ENCONTRADA:", companyLocation);

    if (!companyLocation) {
      return res.status(400).json({
        message: "Esta empresa não tem nenhuma localização associada"
      });
    }

    const durationNumber = Number(duration_in_months);
    const totalSpotsNumber = Number(total_spots);
    const deadlineDate = new Date(application_deadline);

    if (isNaN(durationNumber)) {
      return res.status(400).json({
        message: "duration_in_months inválido"
      });
    }

    if (isNaN(totalSpotsNumber)) {
      return res.status(400).json({
        message: "total_spots inválido"
      });
    }

    if (isNaN(deadlineDate.getTime())) {
      return res.status(400).json({
        message: "application_deadline inválido"
      });
    }

    const offer = {
      name: name.trim(),
      description: description.trim(),
      requirements: requirements.trim(),

      durationInMonths: durationNumber,
      totalSpots: totalSpotsNumber,
      applicationDeadline: deadlineDate,

      isActive: true,

      // Usa o nome real da empresa que está na BD
      companyName: company.name,

      // Isto é o texto/localização mostrado na oferta
      location: location.trim(),

      workModel: "Presencial",

      industryId: industry._id,
      companyLocationId: companyLocation._id,

      createdAt: new Date(),
      updatedAt: new Date()
    };

    console.log("OFERTA A INSERIR:", offer);

    const result = await db.collection("internshipOffers").insertOne(offer);

    console.log("OFERTA INSERIDA COM ID:", result.insertedId.toString());

    res.status(201).json({
      message: "Oferta criada com sucesso",
      insertedId: result.insertedId.toString()
    });
  } catch (error) {
    console.error("ERRO AO CRIAR OFERTA:", error);
    console.error("STACK:", error.stack);
    console.error(
      "DETALHES VALIDAÇÃO:",
      JSON.stringify(error.errInfo?.details, null, 2)
    );

    res.status(500).json({
      message: "Erro ao criar oferta de estágio",
      error: error.message,
      details: error.errInfo?.details || null
    });
  }
});



app.post("/applications", upload.single("cv"), async (req, res) => {
  let documentInsertedId = null;

  try {
    console.log("====== RECEBI /applications ======");
    console.log("BODY:", req.body);
    console.log("FILE:", req.file);
    const {
      userId,
      internshipOfferId,
      availableFrom
    } = req.body;

    const responderErro = (status, message) => {
      console.log("ERRO CONTROLADO:", status, message);
      apagarFicheiro(req.file?.path);
      return res.status(status).json({ message });
    };

    if (!req.file) {
      return responderErro(400, "Currículo obrigatório.");
    }

    if (!userId) {
      return responderErro(400, "userId é obrigatório.");
    }

    if (!internshipOfferId) {
      return responderErro(400, "internshipOfferId é obrigatório.");
    }

    if (!ObjectId.isValid(userId)) {
      return responderErro(400, "userId inválido.");
    }

    if (!ObjectId.isValid(internshipOfferId)) {
      return responderErro(400, "internshipOfferId inválido.");
    }

    const userObjectId = new ObjectId(userId);
    const offerObjectId = new ObjectId(internshipOfferId);

    const user = await db.collection("users").findOne({
      _id: userObjectId
    });

    if (!user) {
      return responderErro(404, "Utilizador não encontrado.");
    }

    const role = await db.collection("roles").findOne({
      _id: user.roleId
    });

    if (role?.name !== "student") {
      return responderErro(403, "Apenas estudantes podem candidatar-se.");
    }

    const student = await db.collection("students").findOne({
      userId: userObjectId
    });

    if (!student) {
      return responderErro(404, "Estudante não encontrado.");
    }

    const offer = await db.collection("internshipOffers").findOne({
      _id: offerObjectId
    });

    if (!offer) {
      return responderErro(404, "Oferta de estágio não encontrada.");
    }

    const existingApplication = await db.collection("applications").findOne({
      studentId: student._id,
      internshipOfferId: offerObjectId
    });

    if (existingApplication) {
      return responderErro(409, "Já existe uma candidatura para esta oferta.");
    }

    const availableFromDate = availableFrom
      ? new Date(availableFrom)
      : new Date();

    if (isNaN(availableFromDate.getTime())) {
      return responderErro(400, "availableFrom inválido.");
    }

    const document = {
      fileName: req.file.originalname,
      filePath: `/uploads/cvs/${req.file.filename}`,
      fileSize: req.file.size,
      category: "cv",
      uploadedAt: new Date()
    };

    const documentResult = await db.collection("documents").insertOne(document);
    documentInsertedId = documentResult.insertedId;
    console.log("DOCUMENTO CRIADO:", documentInsertedId.toString());

    const application = {
      appliedDate: new Date(),
      status: "pending",
      coverLetter: "",
      availableFrom: availableFromDate,
      cvDocumentId: documentInsertedId,
      studentId: student._id,
      internshipOfferId: offerObjectId
    };

    const result = await db.collection("applications").insertOne(application);
    console.log("CANDIDATURA CRIADA:", result.insertedId.toString());

    res.status(201).json({
      message: "Candidatura submetida com sucesso.",
      application: {
        id: result.insertedId.toString(),
        status: application.status,
        cvDocumentId: documentInsertedId.toString()
      }
    });
  } catch (error) {
    console.error("ERRO AO CRIAR CANDIDATURA:", error);
    console.error("DETALHES:", JSON.stringify(error.errInfo?.details, null, 2));

    if (documentInsertedId) {
      await db.collection("documents").deleteOne({
        _id: documentInsertedId
      });
    }

    apagarFicheiro(req.file?.path);

    res.status(500).json({
      message: "Erro ao criar candidatura",
      error: error.message
    });
  }
});

app.get("/student-applications", async (req, res) => {
  try {
    console.log("QUERY /student-applications:", req.query);


    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({
        message: "userId é obrigatório"
      });
    }

    if (!ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const student = await db.collection("students").findOne({
      userId: new ObjectId(userId)
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
        $lookup: {
          from: "documents",
          localField: "cvDocumentId",
          foreignField: "_id",
          as: "cvDocument"
        }
      },
      {
        $unwind: {
          path: "$cvDocument",
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $project: {
          _id: { $toString: "$_id" },
          status: 1,
          appliedDate: 1,
          cvName: {
            $ifNull: ["$cvDocument.fileName", "Sem currículo"]
          },
          cvPath: {
            $ifNull: ["$cvDocument.filePath", null]
          },
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

app.get("/company-applications", async (req, res) => {
  try {
    console.log("QUERY /company-applications:", req.query);

    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({
        message: "userId é obrigatório"
      });
    }

    if (!ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const ownerUserId = new ObjectId(userId);

    const company = await db.collection("companies").findOne({
      ownerUserId: ownerUserId
    });

    if (!company) {
      return res.json([]);
    }

    const companyLocations = await db.collection("companyLocations").find({
      $or: [
        { companyId: company._id },
        { companyId: company._id.toString() },
        { company_id: company._id },
        { company_id: company._id.toString() }
      ]
    }).toArray();

    const companyLocationIds = companyLocations.flatMap(location => [
      location._id,
      location._id.toString()
    ]);

    const offers = await db.collection("internshipOffers").find({
      $or: [
        {
          companyLocationId: {
            $in: companyLocationIds
          }
        },
        {
          company_location_id: {
            $in: companyLocationIds
          }
        },
        {
          companyName: company.name
        }
      ]
    }).toArray();

    const offerIds = offers.flatMap(offer => [
      offer._id,
      offer._id.toString()
    ]);

    if (offerIds.length === 0) {
      return res.json([]);
    }

    const applications = await db.collection("applications").aggregate([
      {
        $match: {
          $or: [
            {
              internshipOfferId: {
                $in: offerIds
              }
            },
            {
              internship_offer_id: {
                $in: offerIds
              }
            }
          ]
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
        $lookup: {
          from: "documents",
          localField: "cvDocumentId",
          foreignField: "_id",
          as: "cvDocument"
        }
      },
      {
        $unwind: {
          path: "$cvDocument",
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $lookup: {
          from: "students",
          localField: "studentId",
          foreignField: "_id",
          as: "student"
        }
      },
      {
        $unwind: {
          path: "$student",
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $lookup: {
          from: "users",
          localField: "student.userId",
          foreignField: "_id",
          as: "studentUser"
        }
      },
      {
        $unwind: {
          path: "$studentUser",
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $project: {
          _id: { $toString: "$_id" },
          status: 1,
          appliedDate: 1,
          cvName: {
            $ifNull: ["$cvDocument.fileName", "Sem currículo"]
          },
          cvPath: {
            $ifNull: ["$cvDocument.filePath", null]
          },
          offerTitle: "$offer.name",
          companyName: "$offer.companyName",
          offerDescription: "$offer.description",
          location: "$offer.location",
          studentName: {
            $trim: {
              input: {
                $concat: [
                  { $ifNull: ["$studentUser.firstName", ""] },
                  " ",
                  { $ifNull: ["$studentUser.lastName", ""] }
                ]
              }
            }
          },
          studentEmail: "$studentUser.email"
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
    console.error("ERRO /company-applications:", error);

    res.status(500).json({
      message: "Erro ao buscar candidaturas da empresa",
      error: error.message
    });
  }
});

function separarNomeCompleto(nome) {
  const partes = (nome || "").trim().split(/\s+/);

  const firstName = partes.shift() || "";
  const lastName = partes.join(" ");

  return {
    firstName,
    lastName
  };
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

app.get("/student-dashboard-stats", async (req, res) => {
  try {
    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({
        message: "userId é obrigatório"
      });
    }

    if (!ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const userObjectId = new ObjectId(userId);

    const student = await db.collection("students").findOne({
      userId: userObjectId
    });

    if (!student) {
      return res.json({
        activeApplications: 0,
        acceptedApplications: 0,
        newMessages: 0
      });
    }

    const activeApplications = await db.collection("applications").countDocuments({
      studentId: student._id,
      status: {
        $nin: ["rejected", "cancelled"]
      }
    });

    const acceptedApplications = await db.collection("applications").countDocuments({
      studentId: student._id,
      status: "accepted"
    });

    // Por enquanto fica 0, se ainda não tiveres sistema de mensagens.
    const newMessages = 0;

    res.json({
      activeApplications,
      acceptedApplications,
      newMessages
    });
  } catch (error) {
    res.status(500).json({
      message: "Erro ao buscar estatísticas do aluno",
      error: error.message
    });
  }
});

app.post("/api/auth/register", async (req, res) => {
  try {
    const {
      nome,
      email,
      username,
      password,
      tipo,
      estudante,
      professor,
      empresa
    } = req.body;
    console.log("BODY REGISTER:", req.body);
    console.log("TIPO RECEBIDO:", tipo);
    console.log("DADOS ESTUDANTE:", estudante);
    console.log("DADOS ESTUDANTE:", professor);
    console.log("DADOS ESTUDANTE:", empresa);

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

    if (tipo === "student") {
      if (!estudante || !estudante.numeroAluno || !estudante.curso || !estudante.ano) {
        return res.status(400).json({
          message: "Dados do estudante incompletos"
        });
      }
    }

    if (tipo === "teacher") {
      if (!professor || !professor.numeroProfessor || !professor.departamento) {
        return res.status(400).json({
          message: "Dados do professor incompletos"
        });
      }
    }

    if (tipo === "company") {
      if (
        !empresa ||
        !empresa.nomeEmpresa ||
        !empresa.rua ||
        !empresa.numero ||
        !empresa.cidade ||
        !empresa.codigoPostal ||
        typeof empresa.isHeadquarters !== "boolean"
      ) {
        return res.status(400).json({
          message: "Dados da empresa incompletos. Nome da empresa, rua, número, cidade, código postal e headquarters são obrigatórios."
        });
      }
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
    let createdCompanyId = null;
    let createdAddressId = null;
    let createdCompanyLocationId = null;

    if (tipo === "student") {
      console.log("VOU INSERIR STUDENT");

      try {
        const studentData = {
          userId: userId,
          indexNumber: Number(estudante.numeroAluno),
          studyYear: Number(estudante.ano),
          degreeLevel: "Licenciatura",
          addressId: new ObjectId("6a1ba421a0d6e4ac5d8c263b"),
          mainCvId: null
        };

        console.log("DADOS A INSERIR EM STUDENTS:", studentData);

        const resultStudent = await db.collection("students").insertOne(studentData);

        console.log("STUDENT INSERIDO COM ID:", resultStudent.insertedId);
      } catch (error) {
        console.error("ERRO /api/auth/register:", error);
        console.error("MESSAGE:", error.message);
        console.error("CODE:", error.code);
        console.error("ERR INFO:", JSON.stringify(error.errInfo, null, 2));

        res.status(500).json({
          message: "Erro ao registar utilizador",
          error: error.message,
          code: error.code,
          details: error.errInfo || null
        });
      }
    }

    if (tipo === "teacher") {
  console.log("VOU INSERIR TEACHER");

  try {
    const departmentName = professor.departamento.trim();

    let faculty = await db.collection("faculties").findOne({
      name: departmentName
    });

    if (!faculty) {
      console.log("FACULDADE NÃO EXISTE, VOU CRIAR:", departmentName);

      const address = {
        street: "Morada não definida",
        buildingNumber: "S/N",
        city: "Cidade não definida",
        postalCode: "0000-000",
        createdAt: new Date(),
        updatedAt: new Date()
      };

      const addressResult = await db.collection("addresses").insertOne(address);

      console.log("ADDRESS DA FACULDADE CRIADO:", addressResult.insertedId.toString());

      const facultyResult = await db.collection("faculties").insertOne({
        name: departmentName,
        addressId: addressResult.insertedId,
        createdAt: new Date(),
        updatedAt: new Date()
      });

      faculty = {
        _id: facultyResult.insertedId,
        name: departmentName,
        addressId: addressResult.insertedId
      };

      console.log("FACULDADE CRIADA:", faculty._id.toString());
    }

    const teacherData = {
      userId: userId,
      academicTitle: "Professor",
      facultyId: faculty._id,

      // Mantemos isto se o schema aceitar campos extra
      teacherNumber: Number(professor.numeroProfessor),

      createdAt: new Date(),
      updatedAt: new Date()
    };

    console.log("DADOS A INSERIR EM TEACHERS:", teacherData);

    const resultTeacher = await db.collection("teachers").insertOne(teacherData);

    console.log("TEACHER INSERIDO COM ID:", resultTeacher.insertedId.toString());
  } catch (err) {
    console.error("ERRO AO INSERIR TEACHER:", err);
    console.error("ERRO TEACHER MESSAGE:", err.message);
    console.error("ERRO TEACHER CODE:", err.code);
    console.error("ERRO TEACHER INFO:", JSON.stringify(err.errInfo, null, 2));

    await db.collection("users").deleteOne({
      _id: userId
    });

    throw err;
  }
}
    if (tipo === "company") {
      const company = {
        ownerUserId: userId,
        name: empresa.nomeEmpresa.trim(),
        website: empresa.website || "",
        description: empresa.descricao || "",
        industryIds: [],
        createdAt: new Date(),
        updatedAt: new Date()
      };

      const companyResult = await db.collection("companies").insertOne(company);
      createdCompanyId = companyResult.insertedId;

      const address = {
        street: empresa.rua.trim(),
        buildingNumber: empresa.numero.trim(),
        city: empresa.cidade.trim(),
        postalCode: empresa.codigoPostal.trim(),
        createdAt: new Date(),
        updatedAt: new Date()
      };

      const addressResult = await db.collection("addresses").insertOne(address);
      createdAddressId = addressResult.insertedId;

      const companyLocation = {
        name: empresa.nomeEmpresa.trim(),
        isHeadquarters: empresa.isHeadquarters,
        companyId: createdCompanyId,
        addressId: createdAddressId,
        createdAt: new Date(),
        updatedAt: new Date()
      };

      const companyLocationResult = await db.collection("companyLocations").insertOne(companyLocation);
      createdCompanyLocationId = companyLocationResult.insertedId;

      console.log("EMPRESA CRIADA:", createdCompanyId.toString());
      console.log("ADDRESS CRIADO:", createdAddressId.toString());
      console.log("COMPANY LOCATION CRIADA:", createdCompanyLocationId.toString());
    }

    res.status(201).json({
      message: "Utilizador registado com sucesso",
      user: {
        id: userId.toString(),
        nome: tipo === "company" ? empresa.nomeEmpresa : nome,
        email: emailNormalizado,
        username: usernameNormalizado,
        tipo: tipo,
        roleId: role._id.toString(),

        companyId: createdCompanyId ? createdCompanyId.toString() : null,
        addressId: createdAddressId ? createdAddressId.toString() : null,
        companyLocationId: createdCompanyLocationId ? createdCompanyLocationId.toString() : null
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

    const user = await db.collection("users").findOne({
      email: email
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

app.get("/company-dashboard-stats", async (req, res) => {
  try {
    const { userId } = req.query;

    console.log("====== /company-dashboard-stats ======");
    console.log("USER ID EMPRESA:", userId);

    if (!userId) {
      return res.status(400).json({
        message: "userId é obrigatório"
      });
    }

    if (!ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const ownerUserId = new ObjectId(userId);

    const company = await db.collection("companies").findOne({
      ownerUserId: ownerUserId
    });

    console.log("EMPRESA ENCONTRADA:", company);

    if (!company) {
      return res.json({
        offers: 0,
        receivedApplications: 0,
        pendingApplications: 0
      });
    }

    const companyLocations = await db.collection("companyLocations").find({
      $or: [
        { companyId: company._id },
        { companyId: company._id.toString() },
        { company_id: company._id },
        { company_id: company._id.toString() }
      ]
    }).toArray();

    console.log("LOCALIZAÇÕES DA EMPRESA:", companyLocations);

    const companyLocationIds = companyLocations.flatMap(location => [
      location._id,
      location._id.toString()
    ]);

    const offers = await db.collection("internshipOffers").find({
      $or: [
        {
          companyLocationId: {
            $in: companyLocationIds
          }
        },
        {
          company_location_id: {
            $in: companyLocationIds
          }
        },

        // fallback para ofertas antigas
        {
          companyName: company.name
        }
      ]
    }).toArray();

    console.log("OFERTAS DA EMPRESA:", offers);

    const offerIds = offers.flatMap(offer => [
      offer._id,
      offer._id.toString()
    ]);

    if (offerIds.length === 0) {
      return res.json({
        offers: 0,
        receivedApplications: 0,
        pendingApplications: 0
      });
    }

    const receivedApplications = await db.collection("applications").countDocuments({
      $or: [
        {
          internshipOfferId: {
            $in: offerIds
          }
        },
        {
          internship_offer_id: {
            $in: offerIds
          }
        }
      ]
    });

    const pendingApplications = await db.collection("applications").countDocuments({
      $and: [
        {
          $or: [
            {
              internshipOfferId: {
                $in: offerIds
              }
            },
            {
              internship_offer_id: {
                $in: offerIds
              }
            }
          ]
        },
        {
          status: "pending"
        }
      ]
    });

    const stats = {
      offers: offers.length,
      receivedApplications,
      pendingApplications
    };

    console.log("STATS EMPRESA:", stats);

    res.json(stats);
  } catch (error) {
    console.error("ERRO /company-dashboard-stats:", error);

    res.status(500).json({
      message: "Erro ao buscar estatísticas da empresa",
      error: error.message
    });
  }
});

app.get("/company-offers", async (req, res) => {
  try {
    console.log("QUERY /company-offers:", req.query);

    const { userId } = req.query;

    if (!userId) {
      return res.status(400).json({
        message: "userId é obrigatório"
      });
    }

    if (!ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const ownerUserId = new ObjectId(userId);

    const company = await db.collection("companies").findOne({
      ownerUserId: ownerUserId
    });

    console.log("EMPRESA DAS OFERTAS:", company);

    if (!company) {
      return res.json([]);
    }

    const companyLocations = await db.collection("companyLocations").find({
      $or: [
        { companyId: company._id },
        { companyId: company._id.toString() },
        { company_id: company._id },
        { company_id: company._id.toString() }
      ]
    }).toArray();

    const companyLocationIds = companyLocations.flatMap(location => [
      location._id,
      location._id.toString()
    ]);

    const offers = await db.collection("internshipOffers").find({
      $or: [
        {
          companyLocationId: {
            $in: companyLocationIds
          }
        },
        {
          company_location_id: {
            $in: companyLocationIds
          }
        },
        {
          companyName: company.name
        }
      ]
    }).sort({
      createdAt: -1
    }).toArray();

    res.json(offers);
  } catch (error) {
    console.error("ERRO /company-offers:", error);

    res.status(500).json({
      message: "Erro ao buscar ofertas da empresa",
      error: error.message
    });
  }
});

app.patch("/applications/:id/status", async (req, res) => {
  try {
    const { id } = req.params;
    const { userId, status } = req.body;

    const estadosValidos = ["pending", "accepted", "rejected", "ongoing", "in_progress"];

    if (!ObjectId.isValid(id)) {
      return res.status(400).json({
        message: "applicationId inválido"
      });
    }

    if (!userId || !ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    if (!status || !estadosValidos.includes(status)) {
      return res.status(400).json({
        message: "Estado inválido"
      });
    }

    const companyUserId = new ObjectId(userId);
    const applicationId = new ObjectId(id);

    const user = await db.collection("users").findOne({
      _id: companyUserId
    });

    if (!user) {
      return res.status(404).json({
        message: "Utilizador não encontrado"
      });
    }

    const role = await db.collection("roles").findOne({
      _id: user.roleId
    });

    if (role?.name !== "company") {
      return res.status(403).json({
        message: "Apenas empresas podem alterar o estado das candidaturas"
      });
    }

    const company = await db.collection("companies").findOne({
      ownerUserId: companyUserId
    });

    if (!company) {
      return res.status(404).json({
        message: "Empresa não encontrada"
      });
    }

    const application = await db.collection("applications").findOne({
      _id: applicationId
    });

    if (!application) {
      return res.status(404).json({
        message: "Candidatura não encontrada"
      });
    }

    const offer = await db.collection("internshipOffers").findOne({
      _id: application.internshipOfferId
    });

    if (!offer) {
      return res.status(404).json({
        message: "Oferta não encontrada"
      });
    }

    const companyLocations = await db.collection("companyLocations").find({
      $or: [
        { companyId: company._id },
        { companyId: company._id.toString() },
        { company_id: company._id },
        { company_id: company._id.toString() }
      ]
    }).toArray();

    const companyLocationIds = companyLocations.flatMap(location => [
      location._id?.toString(),
      location._id
    ]);

    const ofertaPertenceEmpresa =
      offer.companyName === company.name ||
      companyLocationIds.some(idLocal =>
        idLocal?.toString() === offer.companyLocationId?.toString() ||
        idLocal?.toString() === offer.company_location_id?.toString()
      );

    if (!ofertaPertenceEmpresa) {
      return res.status(403).json({
        message: "Esta candidatura não pertence a uma oferta desta empresa"
      });
    }

    await db.collection("applications").updateOne(
      { _id: applicationId },
      {
        $set: {
          status: status,
          updatedAt: new Date()
        }
      }
    );

    res.json({
      message: "Estado da candidatura atualizado com sucesso",
      applicationId: id,
      status: status
    });
  } catch (error) {
    console.error("ERRO AO ATUALIZAR CANDIDATURA:", error);

    res.status(500).json({
      message: "Erro ao atualizar estado da candidatura",
      error: error.message
    });
  }
});

app.get("/messages/conversations", async (req, res) => {
  try {
    const { userId } = req.query;

    if (!userId || !ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const userObjectId = new ObjectId(userId);

    const user = await db.collection("users").findOne({
      _id: userObjectId
    });

    if (!user) {
      return res.status(404).json({
        message: "Utilizador não encontrado"
      });
    }

    const role = await db.collection("roles").findOne({
      _id: user.roleId
    });

    let applicationMatch = null;

    if (role?.name === "student") {
      const student = await db.collection("students").findOne({
        userId: userObjectId
      });

      if (!student) {
        return res.json([]);
      }

      applicationMatch = {
        studentId: student._id
      };
    } else if (role?.name === "company") {
      const company = await db.collection("companies").findOne({
        ownerUserId: userObjectId
      });

      if (!company) {
        return res.json([]);
      }

      const companyLocations = await db.collection("companyLocations").find({
        $or: [
          { companyId: company._id },
          { companyId: company._id.toString() },
          { company_id: company._id },
          { company_id: company._id.toString() }
        ]
      }).toArray();

      const companyLocationIds = companyLocations.flatMap(location => [
        location._id,
        location._id.toString()
      ]);

      const offers = await db.collection("internshipOffers").find({
        $or: [
          {
            companyLocationId: {
              $in: companyLocationIds
            }
          },
          {
            company_location_id: {
              $in: companyLocationIds
            }
          },
          {
            companyName: company.name
          }
        ]
      }).toArray();

      const offerIds = offers.flatMap(offer => [
        offer._id,
        offer._id.toString()
      ]);

      if (offerIds.length === 0) {
        return res.json([]);
      }

      applicationMatch = {
        $or: [
          {
            internshipOfferId: {
              $in: offerIds
            }
          },
          {
            internship_offer_id: {
              $in: offerIds
            }
          }
        ]
      };
    } else {
      return res.json([]);
    }

    const applications = await db.collection("applications").aggregate([
      {
        $match: applicationMatch
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
        $unwind: {
          path: "$offer",
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $lookup: {
          from: "students",
          localField: "studentId",
          foreignField: "_id",
          as: "student"
        }
      },
      {
        $unwind: {
          path: "$student",
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $lookup: {
          from: "users",
          localField: "student.userId",
          foreignField: "_id",
          as: "studentUser"
        }
      },
      {
        $unwind: {
          path: "$studentUser",
          preserveNullAndEmptyArrays: true
        }
      },
      {
        $project: {
          _id: { $toString: "$_id" },
          status: 1,
          appliedDate: 1,
          offerTitle: {
            $ifNull: ["$offer.name", "Oferta sem título"]
          },
          companyName: "$offer.companyName",
          studentName: {
            $trim: {
              input: {
                $concat: [
                  { $ifNull: ["$studentUser.firstName", ""] },
                  " ",
                  { $ifNull: ["$studentUser.lastName", ""] }
                ]
              }
            }
          },
          studentEmail: "$studentUser.email"
        }
      },
      {
        $sort: {
          appliedDate: -1
        }
      }
    ]).toArray();

    const conversations = [];

    for (const application of applications) {
      const applicationObjectId = new ObjectId(application._id);

      const lastMessages = await db.collection("messages")
        .find({
          applicationId: applicationObjectId,
          $or: [
            { senderId: userObjectId },
            { receiverId: userObjectId }
          ]
        })
        .sort({ sentAt: -1 })
        .limit(1)
        .toArray();

      const lastMessage = lastMessages[0] || null;

      conversations.push({
        applicationId: application._id,
        offerTitle: application.offerTitle,
        companyName: application.companyName || null,
        studentName: application.studentName || null,
        studentEmail: application.studentEmail || null,
        status: application.status || "pending",
        lastMessage: lastMessage ? lastMessage.content : null,
        lastMessageAt: lastMessage ? lastMessage.sentAt.toISOString() : null
      });
    }

    res.json(conversations);
  } catch (error) {
    console.error("ERRO AO LISTAR CONVERSAS:", error);

    res.status(500).json({
      message: "Erro ao listar conversas",
      error: error.message
    });
  }
});

app.get("/applications/:id/messages", async (req, res) => {
  try {
    const { id } = req.params;
    const { userId } = req.query;

    if (!ObjectId.isValid(id)) {
      return res.status(400).json({
        message: "applicationId inválido"
      });
    }

    if (!userId || !ObjectId.isValid(userId)) {
      return res.status(400).json({
        message: "userId inválido"
      });
    }

    const applicationId = new ObjectId(id);
    const userObjectId = new ObjectId(userId);

    const messages = await db.collection("messages")
      .find({
        applicationId: applicationId,
        $or: [
          { senderId: userObjectId },
          { receiverId: userObjectId }
        ]
      })
      .sort({ sentAt: 1 })
      .toArray();

    res.json(
      messages.map(message => ({
        _id: message._id.toString(),
        applicationId: message.applicationId?.toString() || id,
        senderUserId: message.senderId.toString(),
        receiverUserId: message.receiverId.toString(),
        content: message.content,
        createdAt: message.sentAt.toISOString()
      }))
    );
  } catch (error) {
    console.error("ERRO AO LISTAR MENSAGENS:", error);

    res.status(500).json({
      message: "Erro ao listar mensagens",
      error: error.message
    });
  }
});

app.post("/applications/:id/messages", async (req, res) => {
  try {
    const { id } = req.params;
    const { senderUserId, content } = req.body;

    if (!ObjectId.isValid(id)) {
      return res.status(400).json({
        message: "applicationId inválido"
      });
    }

    if (!senderUserId || !ObjectId.isValid(senderUserId)) {
      return res.status(400).json({
        message: "senderUserId inválido"
      });
    }

    if (!content || content.trim() === "") {
      return res.status(400).json({
        message: "Mensagem vazia"
      });
    }

    const applicationId = new ObjectId(id);
    const senderObjectId = new ObjectId(senderUserId);

    const application = await db.collection("applications").findOne({
      _id: applicationId
    });

    if (!application) {
      return res.status(404).json({
        message: "Candidatura não encontrada"
      });
    }

    const senderUser = await db.collection("users").findOne({
      _id: senderObjectId
    });

    if (!senderUser) {
      return res.status(404).json({
        message: "Utilizador não encontrado"
      });
    }

    const role = await db.collection("roles").findOne({
      _id: senderUser.roleId
    });

    let receiverUserId = null;

    if (role?.name === "student") {
      const student = await db.collection("students").findOne({
        userId: senderObjectId
      });

      if (!student || student._id.toString() !== application.studentId.toString()) {
        return res.status(403).json({
          message: "Esta candidatura não pertence a este aluno"
        });
      }

      const offer = await db.collection("internshipOffers").findOne({
        _id: application.internshipOfferId
      });

      if (!offer) {
        return res.status(404).json({
          message: "Oferta não encontrada"
        });
      }

      let company = null;

      if (offer.companyLocationId) {
        const companyLocation = await db.collection("companyLocations").findOne({
          _id: offer.companyLocationId
        });

        if (companyLocation) {
          company = await db.collection("companies").findOne({
            _id: companyLocation.companyId
          });
        }
      }

      if (!company && offer.companyName) {
        company = await db.collection("companies").findOne({
          name: offer.companyName
        });
      }

      if (!company) {
        return res.status(404).json({
          message: "Empresa da oferta não encontrada"
        });
      }

      receiverUserId = company.ownerUserId;
    } else if (role?.name === "company") {
      const student = await db.collection("students").findOne({
        _id: application.studentId
      });

      if (!student) {
        return res.status(404).json({
          message: "Estudante não encontrado"
        });
      }

      receiverUserId = student.userId;
    } else {
      return res.status(403).json({
        message: "Apenas alunos e empresas podem enviar mensagens"
      });
    }

    const newMessage = {
      content: content.trim(),
      sentAt: new Date(),
      isRead: false,
      senderId: senderObjectId,
      receiverId: receiverUserId,
      applicationId: applicationId
    };

    const result = await db.collection("messages").insertOne(newMessage);

    res.status(201).json({
      _id: result.insertedId.toString(),
      applicationId: applicationId.toString(),
      senderUserId: senderObjectId.toString(),
      receiverUserId: receiverUserId.toString(),
      content: newMessage.content,
      createdAt: newMessage.sentAt.toISOString()
    });
  } catch (error) {
    console.error("ERRO AO ENVIAR MENSAGEM:", error);

    res.status(500).json({
      message: "Erro ao enviar mensagem",
      error: error.message
    });
  }
});

connectToMongo().then(() => {
  app.listen(PORT, () => {
    console.log(`Servidor a correr em http://localhost:${PORT}`);
  });
});